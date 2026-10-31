package com.beaste.macro;

interface IBeastEUserService {
    boolean ping();
    boolean tapKey(int keyCode, long eventTime);
    void stop();
}
