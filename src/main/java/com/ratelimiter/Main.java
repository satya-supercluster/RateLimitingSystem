package com.ratelimiter;

import static java.lang.System.exit;

import com.ratelimiter.demo.RateLimiterDemo;
/*
*  Entry Point
* */
public class Main {
    public static void main(String[] args) {
        try {
            RateLimiterDemo.main(args);
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }
        exit(0);
    }
}