import javax.swing.*;
import java.io.File;
import java.net.InetAddress;
import java.util.Scanner;

public class MainAPP extends JFrame {

    Scanner scanner = new Scanner(System.in);
    FileTransferClient client;
    FileTransferServer server;

    public static void main(String[] args) {
        MainAPP app = new MainAPP();
        while (true) {
            app.ShowMenuView();
        }
    }

    private void ShowMenuView() {
        System.out.println("欢迎使用XXX文件传输工具");
        System.out.print("请选择本计算机的角色：[1]接收方，[2]发送方,[q]退出:");
        String cmd = scanner.next();
        while (true) {
            if(cmd.equals("1")){
                ActAsServer();
            }else if(cmd.equals("2")){
                ActAsSender();
            }else if(cmd.equals("q")){
                System.out.println("感谢您的使用！");
                break;
            } else {
                System.out.print("您的输入非法，请重写输入:");
                cmd = scanner.next();
                continue;
            }
        }
    }

    private void ActAsSender() {
        String address;
        int port;
        System.out.print("请输入目标计算机的网络地址(默认为127.0.0.1)：");
        address = scanner.next();
        if (address.isEmpty()) {
            address = "127.0.0.1";
        }
        System.out.print("请输入接收服务所在的端口号(默认为8899)：");
        port = scanner.nextInt();
        System.out.println("正在连接到[" + address + "]" + ":" + port);
        try {
            client = new FileTransferClient(address, port);
            System.out.println("连接成功!");
            selectFileToSend();
        } catch (Exception e) {
            System.out.println("服务不存在，请检查文件接收端是否启用文件接收服务。");
            e.printStackTrace();
        }
    }

    private void selectFileToSend() {
        System.out.println("请选择您想要传送的文件：");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showDialog(this, "确认");
        File file = fileChooser.getSelectedFile();
        System.out.println("您选择的文件为：[" + file.getAbsolutePath() + "]\n是否发送？(y/n)");
        String cmd;
        while (true) {
            cmd = scanner.next();
            if (cmd.equals("y")) {
                try {
                    client.sendFile(file);
                } catch (Exception e) {
                    System.out.println("文件错误或不存在，请检查您的操作");
                }
            } else if (cmd.equals("n")) {
                break;
            } else {
                System.out.print("您的输入为非法输入，请重写输入(y/n):");
                continue;
            }
        }

    }

    private void ActAsServer() {
        System.out.print("请输入文件接收服务所在端口号：");
        int port;
        while (true) {
            if (!scanner.hasNextInt()) {
                scanner.nextLine();
                System.out.println("您的端口号非法，请输入新的端口号");
                continue;
            } else {
                port = scanner.nextInt();
            }
            break;
        }
        ServerInit(port);
    }

    private void ServerInit(int port) {
        String localIP = "";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            localIP = inetAddress.getHostAddress();
        } catch (Exception e) {
            localIP = "未知";
        }
        try {
            server = new FileTransferServer(port);
            System.out.println("文件接收服务已经启动\n服务所在IP："
                    + localIP + "  \n服务端口：" + server.getLocalPort());
            server.load();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("文件接收服务启动失败，端口号" + port + "被占用！");
        }
    }
}
