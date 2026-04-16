#include <bits/stdc++.h>

class Bar {
private:
    std::array<int, 16> not_important_;
};

class Foo {
public:
    Foo()
        : bar_{new Bar{}} {}

    // Rule of three!
    Foo(const Foo& rhs) {
        // TODO: Not yet implemented.
        bar_ = rhs.bar_ ? new Bar(*rhs.bar_) : nullptr;
    }

    Foo& operator=(const Foo& rhs) {
        // TODO: Not yet implemented.
        if (this == &rhs) {
            return *this;
        }
        Foo tmp(rhs);       
        std::swap(bar_, tmp.bar_);          
        return *this;
    }

    ~Foo() {
        // TODO: Not yet implemented.
        delete bar_;
    }

private:
    // XXX: Well, this is not doing well in practical...
    // but hey, this is just some exercise. Please use
    // std::unique_ptr or std::shared_ptr instead.
    //
    // The Foo object will "own" this Bar pointer.
    Bar* bar_{nullptr};
};

int main() {
    Foo a;       // constructor
    Foo b(a);    // copy constructor

    Foo c;
    c = a;       // assignment operator

    return 0;
}