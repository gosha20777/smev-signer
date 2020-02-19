//
// UTF-8
//
// -------------------------------------------------------------------
// SmevSigner (СМЭВ-3)
// -------------------------------------------------------------------
//
// Пакет классов smevsign для трансформации и подписания XML по методическим рекомендациям СМЭВ 3 взят отсюда:
//   https://github.com/Twayn/DigitalSignature
//     + внесены некоторые корректировки под текущую версию JRE
// @author gosha20777
//

import java.util.*;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.soap.SOAPMessage;
import com.fasterxml.uuid.Generators;
import static java.nio.charset.StandardCharsets.UTF_8;
import static smevsign.KeyStoreWrapper.getPrivateKey;
import static smevsign.KeyStoreWrapper.getX509Certificate;
import smevsign.SignAttributesSupplier;
import smevsign.Signer;

public class SmevSigner
{
    private static String KEY_ALIAS;
    private static String KEY_PASS;
    private static String TAG_FOR_SIGN;

    public static void main(String[] args)
    {
        String XML_WRAPPER = "";
        String XML_INPUT = "";
        String XML_OUTPUT = "";

        // определяем путь приложения
        String APP_PATH = SmevSigner.class.getProtectionDomain().getCodeSource().getLocation().getFile().replaceAll("^/","");

        // берем настройки из config.ini
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(new File("/"+APP_PATH+"config.ini")));
            KEY_ALIAS = props.getProperty("KEY_ALIAS");
            KEY_PASS = props.getProperty("KEY_PASS");
        } catch (Exception e) {
            System.out.println("Error. Configuration file is not found:\n /"+APP_PATH+"config.ini");
            System.exit(0);
        }

        // проверяем кол-во входных параметров
        if (args.length < 5) {
            System.out.println("Error. Not enough input params in arguments line.\n\nUsage:\n java -classpath . SmevSigner <MSG_TYPE> <TAG_FOR_SIGN> <INPUT_FILE> <OUTPUT_FILE>\n\nWhere:\n MSG_TYPE - type of message to SMEV-3 (SendRequestRequest, GetResponseRequest, AckRequest)\n TAG_FOR_SIGN - ID of the element in XML to be signed (SIGNED_BY_CONSUMER, SIGNED_BY_CALLER)\n MESSAGE_ID - time-based UUID or type '0' for generating a new one\n INPUT_FILE - path to file with message to input\n OUTPUT_FILE - ready signed file for SMEV-3");
            System.exit(0);
        }

        // метод / тип сообщения к СМЭВ : SendRequestRequest, GetResponseRequest, AckRequest
        String MSG_TYPE = args[0];

        // ID элемента в XML на который следует поставить подпись
        TAG_FOR_SIGN = args[1];

        // UUID сообщения
        String MESSAGE_ID = args[2];

        // путь к файлу для подписания
        String INPUT_FILE = args[3];

        // путь к файлу для результата
        String OUTPUT_FILE = args[4];

        // проверяем входящий файл и читаем его в строку
        File input_file = new File(INPUT_FILE);
        if (!input_file.isFile()) {
            System.out.println("Error. File for signing is not found:\n"+INPUT_FILE);
            System.exit(0);
        }
        try {
            FileInputStream fis = new FileInputStream(input_file);
            byte[] data = new byte[(int) input_file.length()];
            fis.read(data);
            fis.close();
            XML_INPUT = new String(data,"UTF-8");
        } catch (Exception e) {
             System.out.println("Error. Can't read input file content:\n"+INPUT_FILE);
             System.exit(0);
        }

        // проверяем файл с нужной оберткой запроса и читаем его в строку
        File wrapper_file = new File(APP_PATH+"wrap-"+MSG_TYPE+".inc");
        if (!wrapper_file.isFile()) {
            System.out.println("Error. Wrapper file is not found for this MSG_TYPE. Check path to file or MSG_TYPE:\n "+APP_PATH+"wrap-"+MSG_TYPE+".inc");
            System.exit(0);
        }
        try {
            FileInputStream fis = new FileInputStream(wrapper_file);
            byte[] data = new byte[(int) wrapper_file.length()];
            fis.read(data);
            fis.close();
            XML_WRAPPER = new String(data,"UTF-8");
        } catch (Exception e) {
             System.out.println("Error. Can't read wrapper file content:\n "+APP_PATH+"wrap-"+MSG_TYPE+".inc");
             System.exit(0);
        }

        // оборачиваем входящее сообщение в обертку
        // и далее оперируем XML_INPUT
        XML_INPUT = XML_WRAPPER.replace("#REQUEST_BODY#",XML_INPUT);

        // берем входящий MessageID или генерируем новый
        if ( MESSAGE_ID.equals("0") ) {
            UUID uuid = Generators.timeBasedGenerator().generate();
            MESSAGE_ID = uuid.toString();
        }
        // вставляем MessageID в шаблон
        XML_INPUT = XML_INPUT.replace("#MESSAGE_ID#",MESSAGE_ID);

        // формируем TIMSTAMP и делаем вставку в шаблон
        // пример даты: 2020-01-28T10:09:55.141+03:00
        DateTimeFormatter dtfmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String TIMESTAMP = dtfmt.format(ZonedDateTime.now());
        XML_INPUT = XML_INPUT.replace("#TIMESTAMP#",TIMESTAMP);

        // подписываем и пишем в файл
        try {
            SOAPMessage signed = Signer.sign(XML_INPUT.getBytes(UTF_8), new SignAttributes());
            OutputStream outputStream = new FileOutputStream(OUTPUT_FILE);
            signed.writeTo(outputStream);

            // ok
            System.out.println("Done.");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // интерфейс конфигурации для подписания
    static class SignAttributes implements SignAttributesSupplier
    {
        @Override public X509Certificate x509Certificate() throws Exception {
            return getX509Certificate(KEY_ALIAS);
        }

        @Override public PrivateKey privateKey() throws Exception {
            return getPrivateKey(KEY_ALIAS, KEY_PASS.toCharArray());
        }

        @Override public String forSignElementId() {
            return TAG_FOR_SIGN;
        }
    }


}