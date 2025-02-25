package org.example;

import java.util.List;

/**
 * Класс, который содержит список объектов класса Address.
 */
public class Addresses {

    private List<Address> addresses;
    public Addresses(){}

    public List<Address> getAdresses() {
        return addresses;
    }

    public void setAdresses(List<Address> adresses) {
        this.addresses = adresses;
    }
}
