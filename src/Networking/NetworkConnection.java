package Networking;

public interface NetworkConnection {
    public boolean sendMessage(String message);

    public String getIpOfClient();

    public String getOwnUsername();

    public String getUsernameOfClient();

    public String getOwnHexColor();

    public String getHexColorOfClient();

    public long getOwnTimeOfPackage();

    public long getTimeOfPackageOfClient();
}
