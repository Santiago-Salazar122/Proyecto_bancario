package com.bank.domain.model.client;

import com.bank.domain.valueobject.Address;
import com.bank.domain.valueobject.Email;
import com.bank.domain.valueobject.PhoneNumber;

/**
 * Base entity for common client contact data.
 */
public abstract class Client {

    private Email email;
    private PhoneNumber phoneNumber;
    private Address address;

    protected Client(Email email, PhoneNumber phoneNumber, Address address,
                     String emailRequiredMessage, String addressRequiredMessage) {
        if (email == null) {
            throw new IllegalArgumentException(emailRequiredMessage);
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("The phone number is required.");
        }
        if (address == null) {
            throw new IllegalArgumentException(addressRequiredMessage);
        }

        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public Email getEmail() {
        return email;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void updateContactInfo(Email newEmail, PhoneNumber newPhoneNumber, Address newAddress) {
        if (newEmail != null) {
            this.email = newEmail;
        }
        if (newPhoneNumber != null) {
            this.phoneNumber = newPhoneNumber;
        }
        if (newAddress != null) {
            this.address = newAddress;
        }
    }
}