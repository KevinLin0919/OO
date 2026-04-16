#include <bits/stdc++.h>

using namespace std;

class Employee {
public:
    Employee(const char *name, int id);
    Employee(const Employee &other);
    Employee& operator=(const Employee &other);
    ~Employee();
    const char* getName() const { return _name; }
private:
    int _id;
    char *_name;
};

Employee::Employee(const char *name, int id) {
    _id = id;
    _name = new char[strlen(name) + 1];
    strcpy(_name, name);
}

Employee::Employee(const Employee &other) {
    _id = other._id;
    _name = new char[strlen(other._name) + 1];
    strcpy(_name, other._name);
}

Employee& Employee::operator=(const Employee &other) {
    if (this == &other) return *this; 
    delete[] _name; 
    _id = other._id;
    _name = new char[strlen(other._name) + 1];
    strcpy(_name, other._name);
    return *this;
}

Employee::~Employee() {
    delete[] _name;
}

int main() {
    Employee programmer("John", 22);
    cout << programmer.getName() << endl;
    Employee manager = programmer;
    cout << manager.getName() << endl;
    return 0;
}