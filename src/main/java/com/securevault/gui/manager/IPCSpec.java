package com.securevault.gui.manager;

public record IPCSpec(int iterations, int keyLength, int tagLength, int ivLength, String secretKeyFactoryAlgorithm,
                      String secretKeySpecAlgorithm, String cipherTransformation,
                      String password, String salt) {
}