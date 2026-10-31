
package com.beaste.macro;

interface IBeastEUserService {
    boolean sendKey(String keyCode);
    boolean ping();
    void stop();
    void destroy();
}
