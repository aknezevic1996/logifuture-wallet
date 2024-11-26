package org.aknezevic.wallet;

import org.aknezevic.wallet.controller.WalletController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WalletAppTest {

    @Autowired
    WalletController controller;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(controller);
    }

}
