package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * В этом классе тестируется sftp клиент
 */
public class TestSFTP {

    public static void main(String[] args) throws IOException {

        boolean workMain = true;
        SftpClient sftpClient= new SftpClient();
        sftpClient.establishConnectionToSftp();
        while (SftpClient.workMain) {

            String action = sftpClient.displaySetOfActions();
            Map<String, String> treeMap = sftpClient.readJsonAndPutToMap();
            sftpClient.executeAction(action, treeMap);
        }
    }

        public static void printMap (Map < String, String > map){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
        }
    }

/**
 * Класс {@code SftpClient} предоставляет реализацию функций по взаимодействию с sftp сервером
 */
class SftpClient {
    /**
     * Поля, необходимые для подключения к sftp серверу
     */
    private String username;
    private String password;
    private String remoteHost;
    private int portSftp;
    private String localPath;
    private String fileName;
    private String sftpPath;


    /**
     * Действия, которые может ввести пользователь в консоль
     */
    private static final String actionGetAllElements = "Получить все элементы";
    private static final String actionGetIpByDomain = "Получить ip адрес по домену";
    private static final String actionGetDomainByIp = "Получить домен по ip адресу";
    private static final String actionAddNewEntry = "Добавить пару домен-адрес";
    private static final String actionDeleteIpByDomain = "Удалить элемент по домену";
    private static final String actionDeleteDomainByIp = "Удалить элемент по адресу";
    /**
     * Регулярное выражение, по которому проверяется ip адрес
     */
    private static final String IPv4Pattern = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
    /**
     * Отвечает за завершение работы программы
     */
    public static boolean workMain = true;

    public SftpClient() {}

    /**
     * Выводит в консоль хэш-таблицу
     * @param map хэш-таблица
     */
    public static void printMap(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    /**
     * Отображает пользователю основные функции по взаимодействию с сервером
     * @return действие, которое пользователь ввёл в консоль
     */
    public String displaySetOfActions(){
        System.out.println("Для завершения работы в любой момент введите: end \n" +
                "Введите действие, которое хотите выполнить:\n" +
                "1) " + actionGetAllElements + "\n" +
                "2) " + actionGetIpByDomain + "\n" +
                "3) " + actionGetDomainByIp + "\n" +
                "4) " + actionAddNewEntry + "\n" +
                "5) " + actionDeleteIpByDomain + "\n" +
                "6) " + actionDeleteDomainByIp);
        Scanner scanner = new Scanner(System.in);
        String action = scanner.nextLine();
        checkWorkMain(action);
        return action;
    }

    /**
     * Испоняет логику, которую ввёл пользователь в консоль
     * @param action действие, которое хочет произвести пользователь
     * @param treeMap хэш таблица, хранящая пары "домен - ip адрес" из файла сервера
     * @throws IOException в случае, если не удастся обновить файл
     */
    public void executeAction(String action, Map<String, String> treeMap) throws IOException {
        Scanner scanner = new Scanner(System.in);
        if (action.equals(actionGetAllElements) || action.equals(actionGetAllElements.toLowerCase())) {
            System.out.println("Список всех элементов");
            printMap(treeMap);
        }
        if (action.equals(actionGetIpByDomain) || action.equals(actionGetIpByDomain.toLowerCase())) {
            System.out.println("Введите доменное имя, которое хотите получить:");
            String domain = scanner.nextLine();
            checkWorkMain(domain);
            if (treeMap.containsKey(domain)) {
                System.out.println("Доменному имени: " + domain + " соответсвует адрес: " + treeMap.get(domain));
            }
            System.out.println("Такого домена нет в файле");
        }
        if (action.equals(actionGetDomainByIp) || action.equals(actionGetDomainByIp.toLowerCase())) {
            System.out.println("Введите ip адрес, домен которого хотите получить:");
            String ip = scanner.nextLine();
            checkWorkMain(ip);
            if (treeMap.containsValue(ip)) {
                for (Map.Entry<String, String> entry : treeMap.entrySet()) {
                    if (entry.getValue().equals(ip)) {
                        System.out.println("Данному ip адресу: " + ip + " соответсвует домен: " + entry.getKey());
                    }
                }
            } else {
                System.out.println("Такого ip адреса нет в файле");
            }
        }
        if (action.equals(actionAddNewEntry) || action.equals(actionAddNewEntry.toLowerCase())) {
            boolean ipMatchesPatternIPv4 = true;
            while (ipMatchesPatternIPv4) {
                System.out.println("Введите домен, который хотите добавить");
                String domain = scanner.nextLine();
                checkWorkMain(domain);
                System.out.println("Введите ip адрес, который хотите добавить");
                String ip = scanner.nextLine();
                checkWorkMain(ip);
                if (!treeMap.containsKey(domain) && !treeMap.containsValue(ip)) {
                    if (ip.matches(IPv4Pattern)) {
                        treeMap.put(domain, ip);
                        System.out.println("Домен и ip добавлены успешно\n" +
                                "Список всех элементов: ");
                        printMap(treeMap);
                        ipMatchesPatternIPv4 = false;
                        updateJsonFile(treeMap);
                        putNewJsonFileOnServer();
                    } else {
                        System.out.println("Вы ввели некорректный ip адрес");
                    }
                } else {
                    System.out.println("Доменное имя или адрес уже находятся в файле");
                }
            }
        }
        if (action.equals(actionDeleteIpByDomain) || action.equals(actionDeleteIpByDomain.toLowerCase())) {
            System.out.println("Введите доменное имя, по которому хотите удалить ip адрес");
            String domain = scanner.nextLine();
            checkWorkMain(domain);
            if (treeMap.containsKey(domain)) {
                treeMap.remove(domain);
                updateJsonFile(treeMap);
                putNewJsonFileOnServer();
                printMap(treeMap);
            } else {
                System.out.println("Такого доменного имени нет ");
            }
        }
        if (action.equals(actionDeleteDomainByIp) || action.equals(actionDeleteDomainByIp.toLowerCase())) {
            System.out.println("Введите ip адрес, по которому хотите удалить доменное имя");
            String ip = scanner.nextLine();
            checkWorkMain(ip);
            if (treeMap.containsValue(ip)) {
                Set<String> setKey = treeMap.keySet();
                for (String key : setKey) {
                    if (treeMap.get(key).equals(ip)) {
                        treeMap.remove(key);
                        updateJsonFile(treeMap);
                        putNewJsonFileOnServer();
                        printMap(treeMap);
                    }
                }
            } else {
                System.out.println("Такого ip адреса нет");
            }
        }
    }

    /**
     * Читает json файл и помещает в хэш-таблицу, где ключом является домен, а значением ip-адрес
     * @return хэш-таблицу, хранящую пары "домен - ip адрес" из файла сервера
     * @throws IOException в случае, если не удалось прочитать данные из файла
     */
    public Map<String, String> readJsonAndPutToMap() throws IOException {
        String PathToJsonFile = localPath + "/" + fileName;
        ObjectMapper objectMapper = new ObjectMapper();
        File addressJsonFile = new File(PathToJsonFile);
        Set<Address> listOfAddress = objectMapper.readValue(addressJsonFile, new TypeReference<Set<Address>>() {
        });
        Map<String, String> treeMapIpByDomain = new TreeMap<>();
        for (Address address : listOfAddress) {
            treeMapIpByDomain.put(address.getDomain(), address.getIp());
        }

        return treeMapIpByDomain;
    }

    /**
     * Обновляет json файл, вызывается в случае, если пользователь вводил в консоль действия
     * по добавлению или удалению пар "домен - адрес"
     * @param map хэш таблица, хранящая пары "домен - ip адрес" из файла сервера
     * @throws IOException в случае, если не удалось записать в файл новые данные
     */
    public void updateJsonFile(Map<String, String> map) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String PathToJsonFile = localPath  + "/"+ fileName;
        File addressJsonFile = new File(PathToJsonFile);
        List<Address> addressesInJsonFile = new ArrayList<>();
        for (Map.Entry<String, String> entry: map.entrySet()){
            addressesInJsonFile.add(new Address(entry.getKey(), entry.getValue()));
        }
        Addresses addresses = new Addresses();
        addresses.setAdresses(addressesInJsonFile);
        objectMapper.writeValue(addressJsonFile, addresses.getAdresses());
    }

    /**
     * После подключения к серверу метод проверяет все строки, которые пользоваетль ввёл в консоль.
     * Если строка равняется "end", проихсодит завершение работы программы
     * @param action действие, которое вводит пользователь в консоль
     */
    public void checkWorkMain(String action){
        if (action.equals("end")){
            workMain = false;
            System.out.println("Завершение работы");

        }
    }

    /**
     * Устанавливает соединение с сервером, принимая  адрес, порт, имя и пароль хоста
     */
    public void establishConnectionToSftp(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите адрес хоста");
        remoteHost = scanner.nextLine();
        System.out.println("Введите номер порта");
        portSftp = Integer.parseInt(scanner.nextLine());
        System.out.println("Введите имя");
        username = scanner.nextLine();
        System.out.println("Введите пароль");
        password = scanner.nextLine();

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, remoteHost, portSftp);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);

            session.connect();

            Channel channel = session.openChannel("sftp");
            ChannelSftp channelSftp = (ChannelSftp) channel;
            channelSftp.connect();
            System.out.println("Соединение с сервером установлено\n Укажите путь до файла, который хотите получить");
            sftpPath = scanner.nextLine();
            System.out.println("Укажите путь до папки с ресурсами, куда будет отправлен файл");
            localPath = scanner.nextLine();
            System.out.println("Укажите имя файла");
            fileName = scanner.nextLine();

            try {
                channelSftp.get(sftpPath + "/" + fileName, localPath);
                channel.disconnect();
                session.disconnect();
            } catch (SftpException e) {
                throw new RuntimeException(e);
            }
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Устанваливает соединение с сервером и помещает новые данные в json файл.
     * Вызывается в том случае, если пользователь вызывал действия по изменению данных в файле
     */
    public void putNewJsonFileOnServer(){
        Scanner scanner = new Scanner(System.in);
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, remoteHost, portSftp);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);

            session.connect();

            Channel channel = session.openChannel("sftp");
            ChannelSftp channelSftp = (ChannelSftp) channel;
            channelSftp.connect();

            try {
                channelSftp.put(localPath + "/" + fileName, sftpPath);
                System.out.println("Файл успешно отправлен");
                channelSftp.disconnect();
                session.disconnect();
            } catch (SftpException e) {
                throw new RuntimeException(e);
            }
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
    }

}