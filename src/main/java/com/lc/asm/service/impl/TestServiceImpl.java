package com.lc.asm.service.impl;

import com.lc.asm.service.TestService;

public class TestServiceImpl implements TestService {

    @Override
    public void getTest() {
        System.out.println("getTest  out");
    }

    @Override
    public Integer updateTest(Integer id) {
        System.out.println("updateTest  out");
        return id;
    }
}
