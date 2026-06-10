package bgWork.handler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Vector;

import javax.swing.SwingUtilities;

import Define.AreaDefine;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import Listener.CPHActionListener;
import Pack.DragPack;
import Pack.SendText;
import bgWork.InitProcess;
import mod.instance.AssociationLine;
import mod.instance.BasicClass;
import mod.instance.CompositionLine;
import mod.instance.DependencyLine;
import mod.instance.GeneralizationLine;
import mod.instance.GroupContainer;
import mod.instance.UseCase;

public class CanvasPanelHandler extends PanelHandler
{
	Vector <JPanel>		members			= new Vector <>();
	Vector <JPanel>		selectComp		= new Vector <>();
	int					boundShift		= 10;
	Point				portHoverPoint	= null;
	CPHActionListener	cphListener;

	public CanvasPanelHandler(JPanel Container, InitProcess process)
	{
		super(Container, process);
		boundDistance = 10;
		initContextPanel();
		Container.add(this.contextPanel);
	}

	@Override
	void initContextPanel()
	{
		JPanel fphContextPanel = core.getFuncPanelHandler().getContectPanel();
		contextPanel = new JPanel()
		{
			@Override
			protected void paintChildren(Graphics g)
			{
				super.paintChildren(g);
				drawPortHints(g);
			}
		};
		contextPanel.setBounds(
				fphContextPanel.getLocation().x
						+ fphContextPanel.getSize().width + boundShift,
				fphContextPanel.getLocation().y, 800, 600);
		contextPanel.setLayout(null);
		contextPanel.setVisible(true);
		contextPanel.setBackground(Color.WHITE);
		contextPanel.setBorder(new LineBorder(Color.BLACK));
		cphListener = new CPHActionListener(this);
		contextPanel.addMouseListener(cphListener);
		contextPanel.addMouseMotionListener(cphListener);
	}

	public void setPortHoverPoint(Point p)
	{
		portHoverPoint = p;
		contextPanel.repaint();
	}

	private void drawPortHints(Graphics g)
	{
		int funcIdx = core.getCurrentFuncIndex();
		if (funcIdx != 1 && funcIdx != 2 && funcIdx != 3 && funcIdx != 6)
		{
			return;
		}
		if (portHoverPoint == null)
		{
			return;
		}
		final int RADIUS = 10;
		final int DOT_R = 5;
		g.setColor(Color.RED);
		for (int i = 0; i < members.size(); i ++)
		{
			JPanel obj = members.elementAt(i);
			Point loc = obj.getLocation();
			Dimension sz = obj.getSize();
			Point[] ports = {
				new Point(loc.x + sz.width / 2, loc.y),
				new Point(loc.x + sz.width, loc.y + sz.height / 2),
				new Point(loc.x, loc.y + sz.height / 2),
				new Point(loc.x + sz.width / 2, loc.y + sz.height)
			};
			for (Point port : ports)
			{
				if (portHoverPoint.distance(port) <= RADIUS)
				{
					g.fillOval(port.x - DOT_R, port.y - DOT_R,
							DOT_R * 2, DOT_R * 2);
				}
			}
		}
	}

	@Override
	public void ActionPerformed(MouseEvent e)
	{
		switch (core.getCurrentFuncIndex())
		{
			case 0:
				selectByClick(e);
				break;
			case 1:
			case 2:
			case 3:
				break;
			case 4:
			case 5:
				addObject(core.getCurrentFunc(), e.getPoint());
				break;
			default:
				break;
		}
		repaintComp();
	}

	public void ActionPerformed(DragPack dp)
	{
		switch (core.getCurrentFuncIndex())
		{
			case 0:
				selectByDrag(dp);
				break;
			case 1:
			case 2:
			case 3:
			case 6:
				addLine(core.getCurrentFunc(), dp);
				break;
			case 4:
			case 5:
				addObjectByDrag(core.getCurrentFunc(), dp);
				break;
			default:
				break;
		}
		repaintComp();
	}

	public void repaintComp()
	{
		for (int i = 0; i < members.size(); i ++)
		{
			members.elementAt(i).repaint();
		}
		contextPanel.updateUI();
	}

	void selectByClick(MouseEvent e)
	{
		Point clickPt = e.getPoint();

		clearAllLineHighlights();

		for (int i = 0; i < members.size(); i ++)
		{
			int portSide = getClickedPortSide(members.elementAt(i), clickPt);
			if (portSide != -1)
			{
				highlightLinesConnectedTo(members.elementAt(i), portSide);
				repaintComp();
				return;
			}
		}

		boolean isSelect = false;
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(members.elementAt(i), e.getPoint()) == true
					&& isSelect == false)
			{
				switch (core.isFuncComponent(members.elementAt(i)))
				{
					case 0:
						((BasicClass) members.elementAt(i)).setSelect(true);
						selectComp.add(members.elementAt(i));
						isSelect = true;
						break;
					case 1:
						((UseCase) members.elementAt(i)).setSelect(true);
						selectComp.add(members.elementAt(i));
						isSelect = true;
						break;
					case 5:
						Point p = e.getPoint();
						p.x -= members.elementAt(i).getLocation().x;
						p.y -= members.elementAt(i).getLocation().y;
						if (groupIsSelect((GroupContainer) members.elementAt(i),
								p))
						{
							((GroupContainer) members.elementAt(i))
									.setSelect(true);
							selectComp.add(members.elementAt(i));
							isSelect = true;
						}
						else
						{
							((GroupContainer) members.elementAt(i))
									.setSelect(false);
						}
						break;
					default:
						break;
				}
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
		repaintComp();
	}

	int getClickedPortSide(JPanel obj, Point click)
	{
		if (!(obj instanceof BasicClass) && !(obj instanceof UseCase))
		{
			return -1;
		}
		final int RADIUS = 8;
		Point loc = obj.getLocation();
		Dimension sz = obj.getSize();
		AreaDefine ad = new AreaDefine();
		Point[] ports = {
			new Point(loc.x + sz.width / 2, loc.y),
			new Point(loc.x + sz.width, loc.y + sz.height / 2),
			new Point(loc.x, loc.y + sz.height / 2),
			new Point(loc.x + sz.width / 2, loc.y + sz.height)
		};
		int[] sides = { ad.TOP, ad.RIGHT, ad.LEFT, ad.BOTTOM };
		for (int i = 0; i < ports.length; i ++)
		{
			if (click.distance(ports[i]) <= RADIUS)
			{
				return sides[i];
			}
		}
		return -1;
	}

	void clearAllLineHighlights()
	{
		Component[] components = contextPanel.getComponents();
		for (Component c : components)
		{
			if (c instanceof AssociationLine)
			{
				((AssociationLine) c).setHighlight(false);
			}
			else if (c instanceof CompositionLine)
			{
				((CompositionLine) c).setHighlight(false);
			}
			else if (c instanceof GeneralizationLine)
			{
				((GeneralizationLine) c).setHighlight(false);
			}
			else if (c instanceof DependencyLine)
			{
				((DependencyLine) c).setHighlight(false);
			}
		}
	}

	void highlightLinesConnectedTo(JPanel obj, int side)
	{
		Component[] components = contextPanel.getComponents();
		for (Component c : components)
		{
			if (c instanceof AssociationLine)
			{
				AssociationLine line = (AssociationLine) c;
				if (line.isConnectedToPort(obj, side))
				{
					line.setHighlight(true);
				}
			}
			else if (c instanceof CompositionLine)
			{
				CompositionLine line = (CompositionLine) c;
				if (line.isConnectedToPort(obj, side))
				{
					line.setHighlight(true);
				}
			}
			else if (c instanceof GeneralizationLine)
			{
				GeneralizationLine line = (GeneralizationLine) c;
				if (line.isConnectedToPort(obj, side))
				{
					line.setHighlight(true);
				}
			}
			else if (c instanceof DependencyLine)
			{
				DependencyLine line = (DependencyLine) c;
				if (line.isConnectedToPort(obj, side))
				{
					line.setHighlight(true);
				}
			}
		}
	}

	boolean groupIsSelect(GroupContainer container, Point point)
	{
		for (int i = 0; i < container.getComponentCount(); i ++)
		{
			if (core.isGroupContainer(container.getComponent(i)))
			{
				point.x -= container.getComponent(i).getLocation().x;
				point.y -= container.getComponent(i).getLocation().y;
				if (groupIsSelect((GroupContainer) container.getComponent(i),
						point) == true)
				{
					return true;
				}
				else
				{
					point.x += container.getComponent(i).getLocation().x;
					point.y += container.getComponent(i).getLocation().y;
				}
			}
			else if (core.isJPanel(container.getComponent(i)))
			{
				if (isInside((JPanel) container.getComponent(i), point))
				{
					return true;
				}
			}
		}
		return false;
	}

	boolean selectByDrag(DragPack dp)
	{
		if (isInSelect(dp.getFrom()) == true)
		{
			// dragging components
			Dimension shift = new Dimension(dp.getTo().x - dp.getFrom().x,
					dp.getTo().y - dp.getFrom().y);
			for (int i = 0; i < selectComp.size(); i ++)
			{
				JPanel jp = selectComp.elementAt(i);
				jp.setLocation(jp.getLocation().x + shift.width,
						jp.getLocation().y + shift.height);
				if (jp.getLocation().x < 0)
				{
					jp.setLocation(0, jp.getLocation().y);
				}
				if (jp.getLocation().y < 0)
				{
					jp.setLocation(jp.getLocation().x, 0);
				}
			}
			return true;
		}
		if (dp.getFrom().x > dp.getTo().x && dp.getFrom().y > dp.getTo().y)
		{
			// drag right down from to left up
			groupInversSelect(dp);
			return true;
		}
		else if (dp.getFrom().x < dp.getTo().x && dp.getFrom().y < dp.getTo().y)
		{
			// drag from left up to right down
			groupSelect(dp);
			return true;
		}
		return false;
	}

	public void setGroup()
	{
		if (selectComp.size() > 1)
		{
			GroupContainer gContainer = new GroupContainer(core);
			gContainer.setVisible(true);
			Point p1 = new Point(selectComp.elementAt(0).getLocation().x,
					selectComp.elementAt(0).getLocation().y);
			Point p2 = new Point(selectComp.elementAt(0).getLocation().x,
					selectComp.elementAt(0).getLocation().y);
			Point testP;
			for (int i = 0; i < selectComp.size(); i ++)
			{
				testP = selectComp.elementAt(i).getLocation();
				if (p1.x > testP.x)
				{
					p1.x = testP.x;
				}
				if (p1.y > testP.y)
				{
					p1.y = testP.y;
				}
				if (p2.x < testP.x + selectComp.elementAt(i).getSize().width)
				{
					p2.x = testP.x + selectComp.elementAt(i).getSize().width;
				}
				if (p2.y < testP.y + selectComp.elementAt(i).getSize().height)
				{
					p2.y = testP.y + selectComp.elementAt(i).getSize().height;
				}
			}
			p1.x --;
			p1.y --;
			gContainer.setLocation(p1);
			gContainer.setSize(p2.x - p1.x + 2, p2.y - p1.y + 2);
			for (int i = 0; i < selectComp.size(); i ++)
			{
				JPanel temp = selectComp.elementAt(i);
				removeComponent(temp);
				gContainer.add(temp, i);
				temp.setLocation(temp.getLocation().x - p1.x,
						temp.getLocation().y - p1.y);
			}
			addComponent(gContainer);
			selectComp = new Vector <>();
			selectComp.add(gContainer);
			repaintComp();
		}
	}

	public void setUngroup()
	{
		int size = selectComp.size();
		for (int i = 0; i < size; i ++)
		{
			if (core.isGroupContainer(selectComp.elementAt(i)))
			{
				GroupContainer gContainer = (GroupContainer) selectComp
						.elementAt(i);
				Component temp;
				int j = 0;
				while (gContainer.getComponentCount() > 0)
				{
					temp = gContainer.getComponent(0);
					temp.setLocation(
							temp.getLocation().x + gContainer.getLocation().x,
							temp.getLocation().y + gContainer.getLocation().y);
					addComponent((JPanel) temp, j);
					selectComp.add((JPanel) temp);
					gContainer.remove(temp);
					j ++;
				}
				removeComponent(gContainer);
				selectComp.remove(gContainer);
			}
			repaintComp();
		}
	}

	void groupSelect(DragPack dp)
	{
		JPanel jp = new JPanel();
		jp.setLocation(dp.getFrom());
		jp.setSize(Math.abs(dp.getTo().x - dp.getFrom().x),
				Math.abs(dp.getTo().y - dp.getFrom().x));
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(jp, members.elementAt(i)) == true)
			{
				selectComp.add(members.elementAt(i));
				setSelectAllType(members.elementAt(i), true);
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
	}

	void groupInversSelect(DragPack dp)
	{
		JPanel jp = new JPanel();
		jp.setLocation(dp.getTo());
		jp.setSize(Math.abs(dp.getTo().x - dp.getFrom().x),
				Math.abs(dp.getTo().y - dp.getFrom().x));
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(jp, members.elementAt(i)) == false)
			{
				selectComp.add(members.elementAt(i));
				setSelectAllType(members.elementAt(i), true);
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
	}

	boolean isInSelect(Point point)
	{
		for (int i = 0; i < selectComp.size(); i ++)
		{
			if (isInside(selectComp.elementAt(i), point) == true)
			{
				return true;
			}
		}
		return false;
	}

	void addLine(JPanel funcObj, DragPack dPack)
	{
		final int SNAP = 15;
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInsideWithTolerance(members.elementAt(i), dPack.getFrom(), SNAP))
			{
				dPack.setFromObj(members.elementAt(i));
			}
			if (isInsideWithTolerance(members.elementAt(i), dPack.getTo(), SNAP))
			{
				dPack.setToObj(members.elementAt(i));
			}
		}
		if (dPack.getFromObj() == dPack.getToObj()
				|| dPack.getFromObj() == contextPanel
				|| dPack.getToObj() == contextPanel)
		{
			return;
		}
		switch (members.size())
		{
			case 0:
			case 1:
				break;
			default:
				switch (core.isLine(funcObj))
				{
					case 0:
						((AssociationLine) funcObj).setConnect(dPack);
						break;
					case 1:
						((CompositionLine) funcObj).setConnect(dPack);
						break;
					case 2:
						((GeneralizationLine) funcObj).setConnect(dPack);
						break;
					case 3:
						((DependencyLine) funcObj).setConnect(dPack);
						break;
					default:
						break;
				}
				contextPanel.add(funcObj, 0);
				break;
		}
	}

	void addObject(JPanel funcObj, Point point)
	{
		if (members.size() > 0)
		{
			members.insertElementAt(funcObj, 0);
		}
		else
		{
			members.add(funcObj);
		}
		final JPanel obj = members.elementAt(0);
		obj.setLocation(point);
		obj.setVisible(true);
		contextPanel.add(obj, 0);
		obj.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				Point p = SwingUtilities.convertPoint(obj, e.getPoint(),
						contextPanel);
				setPortHoverPoint(p);
			}

			@Override
			public void mouseDragged(MouseEvent e)
			{
				Point p = SwingUtilities.convertPoint(obj, e.getPoint(),
						contextPanel);
				setPortHoverPoint(p);
			}
		});
		obj.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				Point p = SwingUtilities.convertPoint(obj, e.getPoint(),
						contextPanel);
				cphListener.mousePressed(new MouseEvent(contextPanel, e.getID(),
						e.getWhen(), e.getModifiersEx(), p.x, p.y,
						e.getClickCount(), false));
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				Point p = SwingUtilities.convertPoint(obj, e.getPoint(),
						contextPanel);
				cphListener.mouseReleased(new MouseEvent(contextPanel, e.getID(),
						e.getWhen(), e.getModifiersEx(), p.x, p.y,
						e.getClickCount(), false));
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				Point p = SwingUtilities.convertPoint(obj, e.getPoint(),
						contextPanel);
				cphListener.mouseClicked(new MouseEvent(contextPanel, e.getID(),
						e.getWhen(), e.getModifiersEx(), p.x, p.y,
						e.getClickCount(), false));
			}
		});
	}

	void addObjectByDrag(JPanel funcObj, DragPack dp)
	{
		Point from = dp.getFrom();
		Point to = dp.getTo();
		int dx = Math.abs(to.x - from.x);
		int dy = Math.abs(to.y - from.y);
		if (dx <= 5 && dy <= 5)
		{
			return;
		}
		final int MIN_W = 100;
		final int MIN_H_CLASS = 50;
		final int MIN_H_CASE = 40;
		int rawW = Math.abs(to.x - from.x);
		int rawH = Math.abs(to.y - from.y);
		if (funcObj instanceof BasicClass)
		{
			int w = Math.max(rawW, MIN_W);
			int h = Math.max(rawH, MIN_H_CLASS);
			((BasicClass) funcObj).setCustomSize(w, h);
		}
		else if (funcObj instanceof UseCase)
		{
			int w = Math.max(rawW, MIN_W);
			int h = Math.max(rawH, MIN_H_CASE);
			((UseCase) funcObj).setCustomSize(w, h);
		}
		Point location = new Point(Math.min(from.x, to.x),
				Math.min(from.y, to.y));
		addObject(funcObj, location);
	}

	public boolean isInsideWithTolerance(JPanel container, Point point, int tolerance)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		return point.x >= cLocat.x - tolerance && point.y >= cLocat.y - tolerance
				&& point.x <= cLocat.x + cSize.width + tolerance
				&& point.y <= cLocat.y + cSize.height + tolerance;
	}

	public boolean isInside(JPanel container, Point point)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		if (point.x >= cLocat.x && point.y >= cLocat.y)
		{
			if (point.x <= cLocat.x + cSize.width
					&& point.y <= cLocat.y + cSize.height)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isInside(JPanel container, JPanel test)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		Point tLocat = test.getLocation();
		Dimension tSize = test.getSize();
		if (cLocat.x <= tLocat.x && cLocat.y <= tLocat.y)
		{
			if (cLocat.x + cSize.width >= tLocat.x + tSize.width
					&& cLocat.y + cSize.height >= tLocat.y + tSize.height)
			{
				return true;
			}
		}
		return false;
	}

	public JPanel getSingleSelectJP()
	{
		if (selectComp.size() == 1)
		{
			return selectComp.elementAt(0);
		}
		return null;
	}

	public void setContext(SendText tr)
	{
		System.out.println(tr.getText());
		try
		{
			switch (core.isClass(tr.getDest()))
			{
				case 0:
					((BasicClass) tr.getDest()).setText(tr.getText());
					break;
				case 1:
					((UseCase) tr.getDest()).setText(tr.getText());
					break;
				default:
					break;
			}
		}
		catch (Exception e)
		{
			System.err.println("CPH error");
		}
	}

	void addComponent(JPanel comp)
	{
		contextPanel.add(comp, 0);
		members.insertElementAt(comp, 0);
	}

	void addComponent(JPanel comp, int index)
	{
		contextPanel.add(comp, index);
		members.insertElementAt(comp, index);
	}

	public void removeComponent(JPanel comp)
	{
		contextPanel.remove(comp);
		members.remove(comp);
	}

	void setSelectAllType(Object obj, boolean isSelect)
	{
		switch (core.isFuncComponent(obj))
		{
			case 0:
				((BasicClass) obj).setSelect(isSelect);
				break;
			case 1:
				((UseCase) obj).setSelect(isSelect);
				break;
			case 2:
				((AssociationLine) obj).setSelect(isSelect);
				break;
			case 3:
				((CompositionLine) obj).setSelect(isSelect);
				break;
			case 4:
				((GeneralizationLine) obj).setSelect(isSelect);
				break;
			case 5:
				((GroupContainer) obj).setSelect(isSelect);
				break;
			case 6:
				((DependencyLine) obj).setSelect(isSelect);
				break;
			default:
				break;
		}
	}

	public Point getAbsLocation(Container panel)
	{
		Point location = panel.getLocation();
		while (panel.getParent() != contextPanel)
		{
			panel = panel.getParent();
			location.x += panel.getLocation().x;
			location.y += panel.getLocation().y;
		}
		return location;
	}
}
