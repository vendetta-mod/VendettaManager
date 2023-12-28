// IShizukuInstallerService.aidl
package dev.beefers.vendetta.manager.installer.shizuku;

interface IShizukuInstallerService {
    void destroy() = 16777114;
    String installApks(in List<String> apkPaths) = 1;
    void exit() = 2;
    String doSomething() = 3;
}