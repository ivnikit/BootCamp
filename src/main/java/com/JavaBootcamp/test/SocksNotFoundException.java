package com.JavaBootcamp.test;

class SocksNotFoundException extends RuntimeException {

    SocksNotFoundException(Long id) {
        super("Could not find employee " + id);
    }
}