package org.example;

import java.io.Serializable;
import java.util.Objects;

/**
 * Класс, в объекты которого записываются домены и адреса из json файла.
 */
public class Address {
    private String domain;
    private String ip;

    public Address(){}

    public Address(String domain, String ip){
        this.domain = domain;
        this.ip = ip;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "Address{" +
                "domain='" + domain + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Address address = (Address) object;
        return Objects.equals(domain, address.domain) && Objects.equals(ip, address.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, ip);
    }
}
