import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableColumn;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

enum StateOfClient {

    CONNECTED, DISCONNECTED
};
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class ChatClient extends JFrame implements DirectoryService {
    private JPanel panel1;
    private JPanel panel2;
    private JPanel panel3;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextField textField;
    private Calendar calendar;
    private String chat_entry;
    private String group_name;
    DatagramSocket socket_from_group;
    private String DIRECTORY_SERVER_IP;
    private int DIRECTORY_SERVER_PORT;
    private String SERVER_IP;
    private String GROUP_IP;
    private int SERVER_PORT;
    private int GROUP_PORT;
    private int RECIEVING_PORT;
    private int group_message_counter;
    private int message_counter;
    private JButton button_send;
    private JFrame connect;
    private JFrame group_list_frame;
    private JFrame create_group_frame;
    private JTextField name_field;
    private JTextField ip_field;
    private JTextField port_field;
    private JTextField group_name_field;
    private JTextField max_users_field;
    private JPasswordField group_password_field;
    private JPasswordField reenter_group_password_field;
    private JPanel table_panel;
    private JTable table;
    private DatagramSocket socket_to_group;
    private MenuItem menu_connect;
    private MenuItem menu_group_list;
    private MenuItem menu_disconnect;
    private MenuItem menu_create_group;
    private Boolean running;
    private String name_field_entry;
    private String ip_field_entry;
    private String port_field_entry;
    private String pw_field_entry;
    private JFrame fonts_color_frame;
    private JFrame background_color_frame;
    private JColorChooser font_color_chooser;
    private JColorChooser background_color_chooser;
    private Color background_color;
    private Color font_color;
    private JPasswordField password;
    private MenuItem menu_about;
    private JFrame about;
    private MenuItem menu_fonts_color;
    private MenuItem menu_background_color;
    private StateOfClient state;
    private MenuItem menu_sounds;
    private JFrame sounds_frame;
    private JComboBox[] choice;
    private JButton[] play;
    private JCheckBox[] checkbox;
    private String[] selected_sounds;
    private int[] selected_sounds_index;
    private Boolean[] enabled_sounds;
    private Settings settings;
    private ArrayList<String> last_used;
    private int last_used_index;
    private int i;
    public static final int NUM_OF_SOUNDS = 6;
    public static final int MAX_SIZE_OF_NAME = 16;
    public static final int NUMBER_OF_LAST_USED = 40;
    public static final int MAX_TIMEOUTS = 15;

    public ChatClient() {
        super();
        setSize(795, 548);
        setTitle("Chat Client BETA");
        setResizable(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        }

        loadSettings();

        background_color = settings.getBackgroundColor();
        getContentPane().setBackground(background_color);

        font_color = settings.getFontColor();

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                disconnect(1);

                System.exit(0);

            }
        });
        
        try {
            BufferedReader br = new BufferedReader(new FileReader("./directory_server.ini"));
            String temp = br.readLine();
            
            if(temp.substring(0, 3).equalsIgnoreCase("IP=")) {
                DIRECTORY_SERVER_IP = temp.substring(3, temp.length());
            }
            else if(temp.substring(0, 5).equalsIgnoreCase("PORT=")) {
                DIRECTORY_SERVER_PORT = Integer.parseInt(temp.substring(5, temp.length()));
            }
            temp = br.readLine();
            if(temp.substring(0, 3).equalsIgnoreCase("IP=")) {
                DIRECTORY_SERVER_IP = temp.substring(3, temp.length());
            }
            else if(temp.substring(0, 5).equalsIgnoreCase("PORT=")) {
                DIRECTORY_SERVER_PORT = Integer.parseInt(temp.substring(5, temp.length()));
            }
            else {
                throw new Exception();
            }
        } 
        catch (FileNotFoundException ex) {
            DIRECTORY_SERVER_IP = "127.0.0.1";
            DIRECTORY_SERVER_PORT = 0;
        }
        catch (IOException ex) {
            DIRECTORY_SERVER_IP = "127.0.0.1";
            DIRECTORY_SERVER_PORT = 0;
        }
        catch (Exception ex) {
            DIRECTORY_SERVER_IP = "127.0.0.1";
            DIRECTORY_SERVER_PORT = 0;
        }
        
        searchRecord("ChatServerUDP");
             
        
        chat_entry = "";
        
        message_counter = 0;
        group_message_counter = -1;

        running = true;
        state = StateOfClient.DISCONNECTED;

        name_field_entry = settings.getNameFieldEntry();
        ip_field_entry = settings.getIpFieldEntry();
        port_field_entry = settings.getPortEntry();

        selected_sounds = new String[NUM_OF_SOUNDS];
        selected_sounds_index = new int[NUM_OF_SOUNDS];
        enabled_sounds = new Boolean[NUM_OF_SOUNDS];

        selected_sounds[0] = settings.getSelectedSounds()[0];
        selected_sounds[1] = settings.getSelectedSounds()[1];
        selected_sounds[2] = settings.getSelectedSounds()[2];
        selected_sounds[3] = settings.getSelectedSounds()[3];
        selected_sounds[4] = settings.getSelectedSounds()[4];
        selected_sounds[5] = settings.getSelectedSounds()[5];

        for (i = 0; i < NUM_OF_SOUNDS; i++) {
            selected_sounds_index[i] = settings.getSelectedSoundsIndex()[i];
            enabled_sounds[i] = settings.getEnabledSounds()[i];
        }

        last_used = new ArrayList<String>(NUMBER_OF_LAST_USED);
        last_used_index = 0;

        KeyListener keylistener = new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        chat_entry = textField.getText();
                        if (chat_entry.length() > 0) {
                            if (state == StateOfClient.DISCONNECTED) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "You are not connected to any group.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());
                                textField.setText("");
                                if (enabled_sounds[5]) {
                                    playSound(selected_sounds[5]);
                                }
                            } else {
                                
                                if(chat_entry.length() > 1500) {
                                    calendar = new GregorianCalendar();
                                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Error, you can only send up to 1500 characters at a time.\n");
                                    textArea1.setCaretPosition(textArea1.getText().length());
                                    if (enabled_sounds[5]) {
                                        playSound(selected_sounds[5]);
                                    }
                                    textField.setText("");
                                    textField.requestFocus();
                                    lastUsedListInsertion();
                                    return;
                                }
                                
                                byte[] buf = new byte[1 + 17 + chat_entry.length() + 1];
                                
                                buf[0] = new Integer(0x0F).byteValue();                              

                                byte[] name_bytes = name_field_entry.getBytes();

                                int j;
                                for (i = 1, j = 0; j < name_bytes.length && j < 17; i++, j++) {
                                    buf[i] = name_bytes[j];
                                }
                                buf[i] = new Integer(0x00).byteValue();

                                byte[] message_bytes = chat_entry.getBytes();

                                for (i = 18, j = 0; j < message_bytes.length; i++, j++) {
                                    buf[i] = message_bytes[j];
                                }
                                buf[i] = new Integer(0x00).byteValue();


                                DatagramPacket packet = null;
                                try {
                                    packet = new DatagramPacket(buf, buf.length, getAddress(GROUP_IP), GROUP_PORT);
                                } 
                                catch (UnknownHostException ex) {
                                    calendar = new GregorianCalendar();
                                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while sending the message.\n");
                                    textArea1.setCaretPosition(textArea1.getText().length());
                                    if (enabled_sounds[5]) {
                                        playSound(selected_sounds[5]);
                                    }
                                    textField.setText("");
                                    textField.requestFocus();
                                    lastUsedListInsertion();
                                    return;
                                }
                                
                                try {
                                    reliable_send(socket_to_group, packet);
                                } 
                                catch (IOException ex) {
                                    calendar = new GregorianCalendar();
                                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while sending the message.\n");
                                    textArea1.setCaretPosition(textArea1.getText().length());
                                    if (enabled_sounds[5]) {
                                        playSound(selected_sounds[5]);
                                    }
                                }

                                textField.setText("");

                            }
                            textField.requestFocus();
                            lastUsedListInsertion();
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (textField.isFocusOwner() && last_used.size() > 0) {
                            textField.setText(last_used.get(last_used_index));
                            try {
                                textField.setCaretPosition(textField.getCaretPosition() + last_used.get(last_used_index).length());
                            }
                            catch(Exception ex) {}
                            last_used_index--;
                            if (last_used_index < 0) {
                                last_used_index = last_used.size() - 1; 
                            }     
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (textField.isFocusOwner() && last_used.size() > 0) {
                            last_used_index++;
                            if (last_used_index > last_used.size() - 1) {
                                last_used_index = 0;
                            } 
                            textField.setText(last_used.get(last_used_index));
                            try {
                                textField.setCaretPosition(textField.getCaretPosition() + last_used.get(last_used_index).length());
                            }
                            catch(Exception ex) {}
                        }
                        break;
                }
            }

            public void keyReleased(KeyEvent e) {}
        };



        panel1 = new JPanel();
        panel2 = new JPanel();
        panel3 = new JPanel();

        setLayout(new FlowLayout());
        textArea1 = new JTextArea(25, 53);
        textArea1.setEditable(false);
        textArea1.setBackground(Color.WHITE);
        textArea1.setFont(new Font("plain", Font.PLAIN, 12));
        textArea1.setForeground(font_color);
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);
        
        JScrollPane textArea1pane = new JScrollPane(textArea1);
        textArea1pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        textArea2 = new JTextArea(25, 9);
        textArea2.setEditable(false);
        textArea2.setFont(new Font("bold", Font.BOLD, 12));
        textArea2.setBackground(Color.WHITE);
        textArea2.setForeground(font_color);

        JScrollPane textArea2pane = new JScrollPane(textArea2);
        textArea2pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //calendar = new GregorianCalendar();
        //textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Type /help to get the available commands.\n");
        //textArea1.setCaretPosition(textArea1.getText().length());

        textField = new JTextField(55);
        textField.setForeground(font_color);
        textField.setFont(new Font("plain", Font.PLAIN, 12));
        textField.requestFocus();
        textField.addKeyListener(keylistener);


        button_send = new JButton("Send");

        button_send.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                chat_entry = textField.getText();
                if (chat_entry.length() > 0) {
                    if (state == StateOfClient.DISCONNECTED) {
                        calendar = new GregorianCalendar();
                        textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "You are not connected to any group.\n");
                        textArea1.setCaretPosition(textArea1.getText().length());
                        textField.setText("");
                        if (enabled_sounds[5]) {
                            playSound(selected_sounds[5]);
                        }
                        } else {
                        
                            if(chat_entry.length() > 1500) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Error, you can only send up to 1500 characters at a time.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());
                                if (enabled_sounds[5]) {
                                    playSound(selected_sounds[5]);
                                }
                                textField.setText("");
                                textField.requestFocus();
                                lastUsedListInsertion();
                                return;
                            }
                                
                            byte[] buf = new byte[1 + 17 + chat_entry.length() + 1];
                            
                            buf[0] = new Integer(0x0F).byteValue();

                            byte[] name_bytes = name_field_entry.getBytes();

                            int j;
                            for (i = 1, j = 0; j < name_bytes.length && j < 17; i++, j++) {
                                buf[i] = name_bytes[j];
                            }
                            buf[i] = new Integer(0x00).byteValue();

                            byte[] message_bytes = chat_entry.getBytes();

                            for (i = 18, j = 0; j < message_bytes.length; i++, j++) {
                                buf[i] = message_bytes[j];
                            }
                            buf[i] = new Integer(0x00).byteValue();


                            DatagramPacket packet = null;
                            try {
                                packet = new DatagramPacket(buf, buf.length, getAddress(GROUP_IP), GROUP_PORT);
                            } 
                            catch (UnknownHostException ex) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while sending the message.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());
                                if(enabled_sounds[5]) {
                                    playSound(selected_sounds[5]);
                                }
                                textField.setText("");
                                textField.requestFocus();
                                lastUsedListInsertion();
                                return;
                            }
                                
                            try {
                                reliable_send(socket_to_group, packet);
                            } 
                            catch (IOException ex) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while sending the message.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());
                                if(enabled_sounds[5]) {
                                    playSound(selected_sounds[5]);
                                }
                            }

                            textField.setText("");

                        }
                        textField.requestFocus();
                        lastUsedListInsertion();
                }
            }
        });

        panel1.add(textArea1pane);
        panel2.add(textArea2pane);
        panel3.add(textField);
        panel3.add(new JLabel("           "));
        panel3.add(button_send);
        panel3.add(new JLabel("           "));



        add(new JLabel("                                                                                               "));
        add(panel1);
        add(new JLabel(""));
        add(panel2);
        add(panel3);
        add(new JLabel("                                     "));

        MenuBar menubar = new MenuBar();

        Menu menu1 = new Menu("File");
        Menu menu2 = new Menu("Options");
        Menu menu3 = new Menu("Help");

        menu_connect = new MenuItem("Join Group");
        
        menu_disconnect = new MenuItem("Leave Group");
        menu_group_list = new MenuItem("Groups List");
        menu_create_group = new MenuItem("Create Group");
        menu_disconnect.setEnabled(false);
        
        menu1.add(menu_create_group);
        menu1.addSeparator();
        menu1.add(menu_group_list);
        menu1.addSeparator();
        menu1.add(menu_connect);
        menu1.addSeparator();
        menu1.add(menu_disconnect);
        menu1.addSeparator();
        MenuItem menu_quit = new MenuItem("Quit");
        menu1.add(menu_quit);

        menu_fonts_color = new MenuItem("Fonts Color");
        menu_background_color = new MenuItem("Background Color");
        menu_sounds = new MenuItem("Sounds");

        menu_about = new MenuItem("About");

        menu_quit.setShortcut(new MenuShortcut(KeyEvent.VK_Q));
        menu_create_group.setShortcut(new MenuShortcut(KeyEvent.VK_R));
        menu_connect.setShortcut(new MenuShortcut(KeyEvent.VK_J));
        menu_disconnect.setShortcut(new MenuShortcut(KeyEvent.VK_L));
        menu_group_list.setShortcut(new MenuShortcut(KeyEvent.VK_G));
        menu_fonts_color.setShortcut(new MenuShortcut(KeyEvent.VK_F));
        menu_background_color.setShortcut(new MenuShortcut(KeyEvent.VK_B));
        menu_sounds.setShortcut(new MenuShortcut(KeyEvent.VK_U));
        menu_about.setShortcut(new MenuShortcut(KeyEvent.VK_A));

        menu2.add(menu_fonts_color);
        menu2.addSeparator();
        menu2.add(menu_background_color);
        menu2.addSeparator();
        menu2.add(menu_sounds);

        menu3.add(menu_about);

        menubar.add(menu1);
        menubar.add(menu2);
        menubar.add(menu3);

        setMenuBar(menubar);

        menu_quit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                disconnect(1);
                System.exit(0);
                
            }
        });

        menu_group_list.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
 
                int group_list_window_width;
                int group_list_window_height;
                
                if(System.getProperty("os.name").equals("Windows XP") || System.getProperty("os.name").equals("Windows 7")) {
                    group_list_window_width = 695;
                    group_list_window_height = 400;
                }
                else {
                    group_list_window_width = 695;
                    group_list_window_height = 425;
                }
             
                group_list_frame = new JFrame("Groups List");
                group_list_frame.setLayout(new FlowLayout());
                group_list_frame.setSize(group_list_window_width, group_list_window_height);
                group_list_frame.setLocation((int) (getLocation().getX() + getSize().getWidth() / 2) - (group_list_window_width / 2), (int) (getLocation().getY() + getSize().getHeight() / 2) - (group_list_window_height / 2));
                group_list_frame.getContentPane().setBackground(background_color);
                group_list_frame.setResizable(false);
                menu_group_list.setEnabled(false);
                setEnabled(false);

                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket();
                } catch (SocketException ex) {
                    calendar = new GregorianCalendar();
                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                    textArea1.setCaretPosition(textArea1.getText().length());
                    menu_group_list.setEnabled(true);
                    if (enabled_sounds[5]) {
                        playSound(selected_sounds[5]);
                    }
                    setEnabled(true);
                    group_list_frame.dispose();
                    return;
                }

                byte[] buf = new byte[3 + (17 + 2 + 2 + 2 + 1) * 200];
                
                buf[0] = new Integer(0x0B).byteValue();


                DatagramPacket packet = null;
                try {
                    packet = new DatagramPacket(buf, buf.length, getAddress(SERVER_IP), SERVER_PORT);
                } 
                catch (UnknownHostException ex) {
                    calendar = new GregorianCalendar();
                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                    textArea1.setCaretPosition(textArea1.getText().length());
                    menu_group_list.setEnabled(true);
                    setEnabled(true);
                    if (enabled_sounds[5]) {
                        playSound(selected_sounds[5]);
                    }
                    group_list_frame.dispose();
                    socket.close();
                    return;
                }
                
                try {
                    socket.send(packet);
                } catch (IOException ex) {
                    calendar = new GregorianCalendar();
                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                    textArea1.setCaretPosition(textArea1.getText().length());
                    menu_group_list.setEnabled(true);
                    setEnabled(true);
                    if (enabled_sounds[5]) {
                        playSound(selected_sounds[5]);
                    }
                    group_list_frame.dispose();
                    socket.close();
                    return;
                }

                packet = new DatagramPacket(buf, buf.length);

                try {
                    socket.setSoTimeout(4000);
                    socket.receive(packet);
                } catch (SocketTimeoutException ex) {
                    calendar = new GregorianCalendar();
                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                    textArea1.setCaretPosition(textArea1.getText().length());
                    menu_group_list.setEnabled(true);
                    setEnabled(true);
                    if (enabled_sounds[5]) {
                        playSound(selected_sounds[5]);
                    }
                    group_list_frame.dispose();
                    socket.close();
                    return;

                } catch (IOException ex) {
                    calendar = new GregorianCalendar();
                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                    textArea1.setCaretPosition(textArea1.getText().length());
                    menu_group_list.setEnabled(true);
                    setEnabled(true);
                    if (enabled_sounds[5]) {
                        playSound(selected_sounds[5]);
                    }
                    group_list_frame.dispose();
                    socket.close();
                    return;
                }

                Object[][] data = null;
                if (new Byte(buf[0]).intValue() == 0x0C) {

                    data = new Object[((buf[1] & 0xFF) << 8) + (buf[2] & 0xFF)][6];

                    for (int k = 0; k < data.length; k++) {

                        data[k][0] = "" + (k + 1);
                                
                        for (i = 0; buf[i + 3 + 24 * k] != 0x00; i++) {
                        }
                        byte[] group_name_bytes = new byte[i];

                        for (i = 0; i < group_name_bytes.length; i++) {
                            group_name_bytes[i] = buf[i + 3 + 24 * k];
                        }
                        data[k][1] = new String(group_name_bytes);

                        data[k][2] = "" + SERVER_IP;

                        data[k][3] = new Integer(((buf[20 + k * 24] & 0xFF) << 8) + (buf[21 + k * 24] & 0xFF));

                        data[k][4] = "" + (((buf[22 + k * 24] & 0xFF) << 8) + (buf[23 + k * 24] & 0xFF)) + "/" + (((buf[24 + k * 24] & 0xFF) << 8) + (buf[25 + k * 24] & 0xFF));

                        data[k][5] = buf[26 + k * 24] == 0x00 ? "No" : "Yes";

                    }
                } else {
                    calendar = new GregorianCalendar();
                    textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                    textArea1.setCaretPosition(textArea1.getText().length());
                    menu_group_list.setEnabled(true);
                    setEnabled(true);
                    if (enabled_sounds[5]) {
                        playSound(selected_sounds[5]);
                    }
                    group_list_frame.dispose();
                    socket.close();
                    return;
                }


                String[] columnNames = {"", "Group Name", "IP Address", "Port Number", "Activity", "Password Protected"};
                
                table = new JTable(data, columnNames);
                TableColumn col = table.getColumnModel().getColumn(0);
                col.setPreferredWidth(5);
                
                col = table.getColumnModel().getColumn(1);
                col.setPreferredWidth(110);

  
                table.setEnabled(false);
                table.setForeground(font_color);
                                  

                JScrollPane tablePane = new JScrollPane(table);
                tablePane.setPreferredSize(new Dimension(640, 300));


                group_list_frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        textField.requestFocus();
                        menu_group_list.setEnabled(true);
                        setEnabled(true);
                        group_list_frame.dispose();
                    }
                });
                
                JButton refresh = new JButton("Refresh");
                refresh.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                     
                        table_panel.removeAll();
                       
                        DatagramSocket socket = null;
                        try {
                            socket = new DatagramSocket();
                        } catch (SocketException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            menu_group_list.setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            setEnabled(true);
                            group_list_frame.dispose();
                            return;
                        }

                        byte[] buf = new byte[3 + (17 + 2 + 2 + 2 + 1) * 200];
                
                        buf[0] = new Integer(0x0B).byteValue();

                        DatagramPacket packet = null;
                        try {
                            packet = new DatagramPacket(buf, buf.length, getAddress(SERVER_IP), SERVER_PORT);
                        } 
                        catch (UnknownHostException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            menu_group_list.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            group_list_frame.dispose();
                            socket.close();
                            return;
                        }
                
                        try {
                            socket.send(packet);
                        } catch (IOException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            menu_group_list.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            group_list_frame.dispose();
                            socket.close();
                            return;
                        }

                        packet = new DatagramPacket(buf, buf.length);

                        try {
                            socket.setSoTimeout(4000);
                            socket.receive(packet);
                        } catch (SocketTimeoutException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            menu_group_list.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            group_list_frame.dispose();
                            socket.close();
                            return;
                        } catch (IOException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            menu_group_list.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            group_list_frame.dispose();
                            socket.close();
                            return;
                        }

                        Object[][] data = null;
                        if (new Byte(buf[0]).intValue() == 0x0C) {

                            data = new Object[((buf[1] & 0xFF) << 8) + (buf[2] & 0xFF)][6];

                            for (int k = 0; k < data.length; k++) {
                                data[k][0] = "" + (k + 1);
                                
                                for (i = 0; buf[i + 3 + 24 * k] != 0x00; i++) {}
                                byte[] group_name_bytes = new byte[i];

                                for (i = 0; i < group_name_bytes.length; i++) {
                                    group_name_bytes[i] = buf[i + 3 + 24 * k];
                                }
                                data[k][1] = new String(group_name_bytes);

                                data[k][2] = "" + SERVER_IP;

                                data[k][3] = new Integer(((buf[20 + k * 24] & 0xFF) << 8) + (buf[21 + k * 24] & 0xFF));

                                data[k][4] = "" + (((buf[22 + k * 24] & 0xFF) << 8) + (buf[23 + k * 24] & 0xFF)) + "/" + (((buf[24 + k * 24] & 0xFF) << 8) + (buf[25 + k * 24] & 0xFF));

                                data[k][5] = buf[26 + k * 24] == 0x00 ? "No" : "Yes";

                           }
                        } else {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has been occured while getting the groups list.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            menu_group_list.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            group_list_frame.dispose();
                            socket.close();
                            return;
                        }


                        String[] columnNames = {"", "Group Name", "IP Address", "Port Number", "Activity", "Password Protected"};
                
                        table = new JTable(data, columnNames);
                        TableColumn col = table.getColumnModel().getColumn(0);
                        col.setPreferredWidth(5);
                
                        col = table.getColumnModel().getColumn(1);
                        col.setPreferredWidth(110);

  
                        table.setEnabled(false);
                        table.setForeground(font_color);
                                  
                        JScrollPane tablePane = new JScrollPane(table);
                        tablePane.setPreferredSize(new Dimension(640, 300));
                
                        table_panel.add(tablePane);
                
                        SwingUtilities.updateComponentTreeUI(table_panel);
                
                        socket.close();
                    }
                });

                
                JButton close = new JButton("Close");
                close.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        menu_group_list.setEnabled(true);
                        textField.requestFocus();
                        setEnabled(true);
                        group_list_frame.dispose();
                    }
                });
                
                table_panel = new JPanel();
                
                table_panel.add(tablePane);
                group_list_frame.add(new JLabel("                                                                 "));
                group_list_frame.add(table_panel);
                group_list_frame.add(refresh);
                group_list_frame.add(close);
               

                group_list_frame.setVisible(true);
                socket.close();
            }
        });

        menu_fonts_color.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                int color_window_width;
                int color_window_height;
                
                if(System.getProperty("os.name").equals("Windows XP") || System.getProperty("os.name").equals("Windows 7")) {
                    color_window_width = 475;
                    color_window_height = 455;
                }
                else {
                    color_window_width = 475;
                    color_window_height = 475;
                }
                
                fonts_color_frame = new JFrame("Fonts Color");
                fonts_color_frame.setLayout(new FlowLayout());
                fonts_color_frame.setSize(color_window_width, color_window_height);
                fonts_color_frame.setLocation((int) (getLocation().getX() + getSize().getWidth() / 2) - (color_window_width / 2), (int) (getLocation().getY() + getSize().getHeight() / 2) - (color_window_height / 2));
                fonts_color_frame.getContentPane().setBackground(background_color);
                fonts_color_frame.setResizable(false);
                menu_fonts_color.setEnabled(false);
                font_color_chooser = new JColorChooser();
                JScrollPane color_scroll_pane = new JScrollPane(font_color_chooser);
                color_scroll_pane.setPreferredSize(new Dimension(435, 360));
                fonts_color_frame.add(new JLabel("                                                      "));
                fonts_color_frame.add(color_scroll_pane);
                setEnabled(false);


                fonts_color_frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        textField.requestFocus();
                        menu_fonts_color.setEnabled(true);
                        setEnabled(true);
                        fonts_color_frame.dispose();
                    }
                });

                JButton confirmButton = new JButton("Confirm");
                confirmButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        font_color = font_color_chooser.getColor();
                        textArea1.setForeground(font_color);
                        textArea2.setForeground(font_color);
                        textField.setForeground(font_color);
                        try {
                            name_field.setForeground(font_color);
                            ip_field.setForeground(font_color);
                            port_field.setForeground(font_color);
                            password.setForeground(font_color);
                            table.setForeground(font_color);
                        } catch (NullPointerException ex) {}

                        settings.setFontColor(font_color);
                        saveSettings();

                        textField.requestFocus();
                        menu_fonts_color.setEnabled(true);
                        setEnabled(true);
                        fonts_color_frame.dispose();
                    }
                });

                JButton close = new JButton("Cancel");
                close.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        textField.requestFocus();
                        menu_fonts_color.setEnabled(true);
                        setEnabled(true);
                        fonts_color_frame.dispose();
                    }
                });

                fonts_color_frame.add(confirmButton);
                fonts_color_frame.add(close);

                fonts_color_frame.setVisible(true);

            }
        });

        menu_background_color.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int color_window_width;
                int color_window_height;
                
                if(System.getProperty("os.name").equals("Windows XP") || System.getProperty("os.name").equals("Windows 7")) {
                    color_window_width = 475;
                    color_window_height = 455;
                }
                else {
                    color_window_width = 475;
                    color_window_height = 475;
                }

                background_color_frame = new JFrame("Background Color");
                background_color_frame.setLayout(new FlowLayout());
                background_color_frame.setSize(color_window_width, color_window_height);
                background_color_frame.setLocation((int) (getLocation().getX() + getSize().getWidth() / 2) - (color_window_width / 2), (int) (getLocation().getY() + getSize().getHeight() / 2) - (color_window_height / 2));
                background_color_frame.getContentPane().setBackground(background_color);
                background_color_frame.setResizable(false);
                menu_background_color.setEnabled(false);
                background_color_chooser = new JColorChooser();
                JScrollPane color_scroll_pane = new JScrollPane(background_color_chooser);
                color_scroll_pane.setPreferredSize(new Dimension(435, 360));
                background_color_frame.add(new JLabel("                                                      "));
                background_color_frame.add(color_scroll_pane);
                setEnabled(false);


                background_color_frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        textField.requestFocus();
                        menu_background_color.setEnabled(true);
                        setEnabled(true);
                        background_color_frame.dispose();
                    }
                });

                JButton confirmButton = new JButton("Confirm");
                confirmButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        textField.requestFocus();
                        background_color = background_color_chooser.getColor();
                        background_color_frame.setBackground(background_color);
                        getContentPane().setBackground(background_color);

                        try {
                            connect.getContentPane().setBackground(background_color);
                        } catch (NullPointerException ex) {}

                        try {
                            sounds_frame.getContentPane().setBackground(background_color);
                        } catch (NullPointerException ex) {}

                        try {
                            fonts_color_frame.getContentPane().setBackground(background_color);
                        } catch (NullPointerException ex) {}

                        try {
                            about.getContentPane().setBackground(background_color);
                        } catch (NullPointerException ex) {}

                        try {
                            group_list_frame.getContentPane().setBackground(background_color);
                        } catch (NullPointerException ex) {}
                        
                        try {
                            create_group_frame.getContentPane().setBackground(background_color);
                        } catch (NullPointerException ex) {}

                        settings.setBackgroundColor(background_color);
                        saveSettings();

                        menu_background_color.setEnabled(true);
                        setEnabled(true);
                        background_color_frame.dispose();
                    }
                });

                JButton close = new JButton("Cancel");
                close.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        textField.requestFocus();
                        menu_background_color.setEnabled(true);
                        setEnabled(true);
                        background_color_frame.dispose();
                    }
                });

                background_color_frame.add(confirmButton);
                background_color_frame.add(close);

                background_color_frame.setVisible(true);

            }
        });

        menu_sounds.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int sounds_window_width;
                int sounds_window_height;
                
                if(System.getProperty("os.name").equals("Windows XP") || System.getProperty("os.name").equals("Windows 7")) {
                    sounds_window_width = 435;
                    sounds_window_height = 315;
                }
                else {
                    sounds_window_width = 500;
                    sounds_window_height = 380;
                }
               
                sounds_frame = new JFrame("Sounds");
                sounds_frame.setLayout(new FlowLayout());
                sounds_frame.setSize(sounds_window_width, sounds_window_height);
                sounds_frame.setLocation((int) (getLocation().getX() + getSize().getWidth() / 2) - (sounds_window_width / 2), (int) (getLocation().getY() + getSize().getHeight() / 2) - (sounds_window_height / 2));
                sounds_frame.getContentPane().setBackground(background_color);
                sounds_frame.setResizable(false);
                menu_sounds.setEnabled(false);
                setEnabled(false);

                JPanel panel8 = new JPanel();
                JPanel panel9 = new JPanel();
                JPanel panel10 = new JPanel();
                JPanel panel11 = new JPanel();
                JPanel panel12 = new JPanel();
                JPanel panel13 = new JPanel();

                panel8.setBackground(Color.LIGHT_GRAY);
                panel9.setBackground(Color.LIGHT_GRAY);
                panel10.setBackground(Color.LIGHT_GRAY);
                panel11.setBackground(Color.LIGHT_GRAY);
                panel12.setBackground(Color.LIGHT_GRAY);
                panel13.setBackground(Color.LIGHT_GRAY);

                choice = new JComboBox[NUM_OF_SOUNDS];
                play = new JButton[NUM_OF_SOUNDS];
                checkbox = new JCheckBox[NUM_OF_SOUNDS];

                for (i = 0; i < play.length; i++) {
                    checkbox[i] = new JCheckBox();
                    checkbox[i].setSelected(enabled_sounds[i]);
                    checkbox[i].addActionListener(new ActionListener() {

                        int temp = i;

                        public void actionPerformed(ActionEvent e) {
                            if (checkbox[temp].isSelected()) {
                                choice[temp].setEnabled(true);
                                play[temp].setEnabled(true);
                            } else {
                                choice[temp].setEnabled(false);
                                play[temp].setEnabled(false);
                            }
                        }
                    });
                }


                panel8.add(checkbox[0]);
                panel8.add(new JLabel("               "));
                panel8.add(new JLabel("Connected"));
                panel8.add(new JLabel("                            "));
                String[] connected_sounds = {"connect1", "connect2", "connect3", "connect4"};
                choice[0] = new JComboBox(connected_sounds);

                panel9.add(checkbox[1]);
                panel9.add(new JLabel("             "));
                panel9.add(new JLabel("Disconnected"));
                panel9.add(new JLabel("                      "));
                String[] disconnected_sounds = {"disconnect1", "disconnect2", "disconnect3"};
                choice[1] = new JComboBox(disconnected_sounds);

                panel10.add(checkbox[2]);
                panel10.add(new JLabel("           "));
                panel10.add(new JLabel("User Connected"));
                panel10.add(new JLabel("                 "));
                String[] user_connected_sounds = {"userConnect1", "userConnect2", "userConnect3", "userConnect4"};
                choice[2] = new JComboBox(user_connected_sounds);

                panel11.add(checkbox[3]);
                panel11.add(new JLabel("         "));
                panel11.add(new JLabel("User Disconnected"));
                panel11.add(new JLabel("           "));
                String[] user_disconnected_sounds = {"userDisconnect1", "userDisconnect2", "userDisconnect3"};
                choice[3] = new JComboBox(user_disconnected_sounds);

                panel12.add(checkbox[4]);
                panel12.add(new JLabel("             "));
                panel12.add(new JLabel("New Message"));
                panel12.add(new JLabel("                  "));
                String[] message_sounds = {"newMessage1", "newMessage2", "newMessage3", "newMessage4", "newMessage5", "newMessage6", "newMessage7", "newMessage8", "newMessage9"};
                choice[4] = new JComboBox(message_sounds);

                panel13.add(checkbox[5]);
                panel13.add(new JLabel("                "));
                panel13.add(new JLabel("Warnings"));
                panel13.add(new JLabel("                              "));
                String[] not_connected_sounds = {"warning1", "warning2", "warning3", "warning4"};
                choice[5] = new JComboBox(not_connected_sounds);


                for (i = 0; i < play.length; i++) {
                    choice[i].setEnabled(enabled_sounds[i]);
                    choice[i].setSelectedIndex(selected_sounds_index[i]);
                    play[i] = new JButton("Play");
                    play[i].setEnabled(enabled_sounds[i]);
                    play[i].addActionListener(new ActionListener() {

                        int temp = i;

                        public void actionPerformed(ActionEvent e) {
                            String selected = (String) choice[temp].getSelectedItem();
                            playSound(selected);
                        }
                    });
                }


                panel8.add(choice[0]);
                panel8.add(new JLabel("       "));
                panel8.add(play[0]);

                panel9.add(choice[1]);
                panel9.add(new JLabel("       "));
                panel9.add(play[1]);

                panel10.add(choice[2]);
                panel10.add(new JLabel("       "));
                panel10.add(play[2]);

                panel11.add(choice[3]);
                panel11.add(new JLabel("       "));
                panel11.add(play[3]);

                panel12.add(choice[4]);
                panel12.add(new JLabel("       "));
                panel12.add(play[4]);

                panel13.add(choice[5]);
                panel13.add(new JLabel("       "));
                panel13.add(play[5]);


                sounds_frame.add(new JLabel("                                  "));
                sounds_frame.add(panel8);
                sounds_frame.add(panel9);
                sounds_frame.add(panel10);
                sounds_frame.add(panel11);
                sounds_frame.add(panel12);
                sounds_frame.add(panel13);



                sounds_frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        textField.requestFocus();
                        menu_sounds.setEnabled(true);
                        setEnabled(true);
                        sounds_frame.dispose();
                    }
                });

                JButton confirmButton = new JButton("Confirm");
                confirmButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (i = 0; i < NUM_OF_SOUNDS; i++) {
                            enabled_sounds[i] = checkbox[i].isSelected();
                            selected_sounds_index[i] = choice[i].getSelectedIndex();
                            selected_sounds[i] = (String) choice[i].getSelectedItem();
                        }

                        settings.setEnabledSounds(enabled_sounds);
                        settings.setSelectedSoundsIndex(selected_sounds_index);
                        settings.setSelectedSounds(selected_sounds);
                        saveSettings();

                        textField.requestFocus();
                        menu_sounds.setEnabled(true);
                        setEnabled(true);
                        sounds_frame.dispose();
                    }
                });

                JButton close = new JButton("Cancel");
                close.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        textField.requestFocus();
                        menu_sounds.setEnabled(true);
                        setEnabled(true);
                        sounds_frame.dispose();
                    }
                });

                sounds_frame.add(confirmButton);
                sounds_frame.add(close);

                sounds_frame.setVisible(true);

            }
        });
        
        menu_create_group.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int create_group_window_width;
                int create_group_window_height;
                
                if(System.getProperty("os.name").equals("Windows XP") || System.getProperty("os.name").equals("Windows 7")) {
                    create_group_window_width = 285;
                    create_group_window_height = 225;
                }
                else {
                    create_group_window_width = 405;
                    create_group_window_height = 280;
                }

                create_group_frame = new JFrame("Create Group");
                create_group_frame.setSize(create_group_window_width, create_group_window_height);
                create_group_frame.setLocation((int) (getLocation().getX() + getSize().getWidth() / 2) - (create_group_window_width / 2), (int) (getLocation().getY() + getSize().getHeight() / 2) - (create_group_window_height / 2));
                create_group_frame.getContentPane().setBackground(background_color);
                create_group_frame.setResizable(false);
                menu_create_group.setEnabled(false);
                setEnabled(false);

                create_group_frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {

                        textField.requestFocus();
                        menu_create_group.setEnabled(true);
                        setEnabled(true);
                        create_group_frame.dispose();

                    }
                });
                
                create_group_frame.setLayout(new FlowLayout());

                
                group_name_field = new JTextField(16);
                max_users_field = new JTextField(4);
                group_password_field = new JPasswordField(16);
                reenter_group_password_field = new JPasswordField(16);
                
                group_name_field.setToolTipText("Group Name, up to 16 characters.");
                max_users_field.setToolTipText("Max Users, 1 to 100.");
                group_password_field.setToolTipText("Password, up to 16 characters.");
                reenter_group_password_field.setToolTipText("Password, up to 16 characters.");
                

                group_name_field.setForeground(font_color);
                max_users_field.setForeground(font_color);
                group_password_field.setForeground(font_color);
                reenter_group_password_field.setForeground(font_color);
                
                JButton createButton = new JButton("Create");
                createButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        int error_found = 0;

                        String group_name;
                        group_name = group_name_field.getText();
                        if(group_name.length() < 1 || group_name.length() > MAX_SIZE_OF_NAME) {
                            group_name_field.setBackground(Color.RED);
                            error_found++;
                        } else {
                            group_name_field.setBackground(Color.WHITE);
                        }
                        
                        int max_users = 0;
                        
                        try {
                            max_users = Integer.parseInt(max_users_field.getText());
                            if( max_users < 1 || max_users > 100) {
                                max_users_field.setBackground(Color.RED);
                                error_found++;
                            } 
                            else {
                                max_users_field.setBackground(Color.WHITE);
                            }
                        }
                        catch(Exception ex) {
                            max_users_field.setBackground(Color.RED);
                            error_found++;
                        }
                           
                        String group_password;
                        group_password = String.copyValueOf(group_password_field.getPassword());
                        
                        if(group_password.length() > 16) {
                            group_password_field.setBackground(Color.RED);
                            error_found++;
                        }
                        else {
                            group_password_field.setBackground(Color.WHITE);
                        }
                        
                        String reenter_group_password;
                        reenter_group_password = String.copyValueOf(reenter_group_password_field.getPassword());
                        
                        if(reenter_group_password.length() > 16) {
                            reenter_group_password_field.setBackground(Color.RED);
                            error_found++;
                        }
                        else {
                            reenter_group_password_field.setBackground(Color.WHITE);
                        }
                        
                        if(!group_password.equals(reenter_group_password) && error_found == 0) {
                            group_password_field.setBackground(Color.RED);
                            reenter_group_password_field.setBackground(Color.RED);
                            error_found++;
                        }
                        else {
                            group_password_field.setBackground(Color.WHITE);
                            reenter_group_password_field.setBackground(Color.WHITE);
                        }
                        
                        if(error_found > 0) {
                            return;
                        }
                        
                        DatagramSocket socket = null;
                        try {
                            socket = new DatagramSocket();
                        } 
                        catch (SocketException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has beed occured while creating the group.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            textField.requestFocus();
                            menu_create_group.setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            setEnabled(true);
                            create_group_frame.dispose();
                            return;
                        }
                               
                        byte[] buf = new byte[1 + 17 + 17 + 2];
                        
                        buf[0] = new Integer(0x07).byteValue();
        
                        byte[] name_bytes = group_name.getBytes();
        
                        int i, j;
                        for(i = 1, j = 0; j < name_bytes.length && i < 17; i++, j++) {
                            buf[i] = name_bytes[j];
                        }
                        buf[i] = new Integer(0x00).byteValue();
        
                        byte[] password_bytes = group_password.getBytes();
        
                        for(i = 18, j = 0; j < password_bytes.length && i < 34; i++, j++) {
                            buf[i] = password_bytes[j];
                        }
                        buf[i] = new Integer(0x00).byteValue();
                        
                        buf[35] = (byte)(max_users >>> 8);
                        buf[36] = (byte)(max_users);
                        
                        DatagramPacket packet = null;
                        try {
                            packet = new DatagramPacket(buf, buf.length, getAddress(SERVER_IP), SERVER_PORT);
                        } 
                        catch (UnknownHostException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has beed occured while creating the group.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            textField.requestFocus();
                            menu_create_group.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            create_group_frame.dispose();
                            socket.close();
                            return;
                        }
                        
                        try {
                            socket.send(packet);
                        } 
                        catch (IOException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has beed occured while creating the group.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            textField.requestFocus();
                            menu_create_group.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            create_group_frame.dispose();
                            socket.close();
                            return;
                        }
        
                        packet = new DatagramPacket(buf, buf.length);
                        try {
                            socket.setSoTimeout(4000);
                            socket.receive(packet);
                        }
                        catch(SocketTimeoutException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has beed occured while creating the group.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            textField.requestFocus();
                            menu_create_group.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            create_group_frame.dispose();
                            socket.close();
                            return;
                        }
                        catch (IOException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has beed occured while creating the group.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            textField.requestFocus();
                            menu_create_group.setEnabled(true);
                            setEnabled(true);
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                            create_group_frame.dispose();
                            socket.close();
                            return;
                        }
        
                        if(new Byte(buf[0]).intValue() == 0x08) {
                            if(new Byte(buf[1]).intValue() == 0x00) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "The group, " + group_name + ", was created successfully.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());
                            }
                            else if(new Byte(buf[1]).intValue() == 0x01) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "The group name, " + group_name + " is already taken.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());
                            }
                            else if(new Byte(buf[1]).intValue() == 0x02) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Not enough space on the server to create the group.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());
                            }  
                        }
                        else {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "An error has beed occured while creating the group.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());
                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }
                        }
                        
                        textField.requestFocus();
                        menu_create_group.setEnabled(true);
                        setEnabled(true);
                        create_group_frame.dispose();
                        socket.close();
                        
                    }
                });
                
                JButton close = new JButton("Cancel");
                close.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        
                        textField.requestFocus();
                        menu_create_group.setEnabled(true);
                        setEnabled(true);
                        create_group_frame.dispose();

                    }
                });

                JPanel panel4 = new JPanel();
                JPanel panel5 = new JPanel();
                JPanel panel6 = new JPanel();
                JPanel panel7 = new JPanel();

                panel4.setBackground(Color.LIGHT_GRAY);
                panel5.setBackground(Color.LIGHT_GRAY);
                panel6.setBackground(Color.LIGHT_GRAY);
                panel7.setBackground(Color.LIGHT_GRAY);
                
                panel4.add(new JLabel("         "));
                panel4.add(new JLabel("Group Name "));
                panel4.add(group_name_field);
                
                panel5.add(new JLabel("            "));
                panel5.add(new JLabel("Max Users "));
                panel5.add(max_users_field);
                panel5.add(new JLabel("                              "));
                
                panel6.add(new JLabel("             "));
                panel6.add(new JLabel("Password "));
                panel6.add(group_password_field);
                

                panel7.add(new JLabel("Re Enter Password "));
                panel7.add(reenter_group_password_field);
 
  
                
                create_group_frame.add(new JLabel("                                      "));
                create_group_frame.add(panel4);
                create_group_frame.add(panel5);
                create_group_frame.add(panel6);
                create_group_frame.add(panel7);
                create_group_frame.add(createButton);
                create_group_frame.add(close);
                create_group_frame.setVisible(true);
                
            }
        });

        menu_connect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int connect_window_width;
                int connect_window_height;
                
                if(System.getProperty("os.name").equals("Windows XP") || System.getProperty("os.name").equals("Windows 7")) {
                    connect_window_width = 280;
                    connect_window_height = 230;
                }
                else {
                    connect_window_width = 370;
                    connect_window_height = 280;
                }

                connect = new JFrame("Connection Options");
                connect.setSize(connect_window_width, connect_window_height);
                connect.setLocation((int) (getLocation().getX() + getSize().getWidth() / 2) - (connect_window_width / 2), (int) (getLocation().getY() + getSize().getHeight() / 2) - (connect_window_height / 2));
                connect.getContentPane().setBackground(background_color);
                connect.setResizable(false);
                menu_connect.setEnabled(false);
                setEnabled(false);

                connect.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        
                        if (name_field_entry.length() < 1 || name_field_entry.length() > MAX_SIZE_OF_NAME) {
                            name_field_entry = "";
                        }

                        try {
                            if (Integer.parseInt(port_field_entry) < 0 || Integer.parseInt(port_field_entry) > 65535) {
                                port_field_entry = "";
                            }
                        } catch (NumberFormatException ex) {
                            port_field_entry = "";
                        }

                        settings.setNameFieldEntry(name_field_entry);
                        settings.setIpFieldEntry(ip_field_entry); 
                        settings.setPortEntry(port_field_entry);
                        saveSettings();

                        textField.requestFocus();
                        menu_connect.setEnabled(true);
                        setEnabled(true);
                        connect.dispose();

                    }
                });

                connect.setLayout(new FlowLayout());

                name_field = new JTextField(16);
                ip_field = new JTextField(16);
                port_field = new JTextField(5);
                password = new JPasswordField(16);
                
                name_field.setToolTipText("User Name, up to 16 characters.");
                ip_field.setToolTipText("IP or DNS Address");
                port_field.setToolTipText("Port, 0 to 65535.");
                password.setToolTipText("Password, up to 16 characters.");


                name_field.setForeground(font_color);
                ip_field.setForeground(font_color);
                port_field.setForeground(font_color);
                password.setForeground(font_color);

                name_field.setText(name_field_entry);
                ip_field.setText(ip_field_entry);
                port_field.setText(port_field_entry);

                JButton connectButton = new JButton("Connect");
                connectButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        int error_found = 0;
                        
                        message_counter = 0;
                        group_message_counter = -1;

                        name_field_entry = name_field.getText();
                        if (name_field_entry.length() < 1 || name_field_entry.length() > MAX_SIZE_OF_NAME) {
                            name_field.setBackground(Color.RED);
                            error_found++;
                        } else {
                            name_field.setBackground(Color.WHITE);
                        }


                        ip_field_entry = ip_field.getText();

                        if (ip_field_entry.length() < 1) {
                            ip_field.setBackground(Color.RED);
                            error_found++;
                        } 
                        else {
                            ip_field.setBackground(Color.WHITE);
                        }
                         

                        if (error_found == 0) {
                            GROUP_IP = ip_field_entry;;
                        }


                        try {
                            port_field_entry = port_field.getText();

                            if (Integer.parseInt(port_field_entry) < 0 || Integer.parseInt(port_field_entry) > 65535) {
                                port_field.setBackground(Color.RED);
                                error_found++;
                            } else {
                                port_field.setBackground(Color.WHITE);
                            }
                        } catch (NumberFormatException ex) {
                            port_field.setBackground(Color.RED);
                            error_found++;
                        }
                        
                        pw_field_entry = String.copyValueOf(password.getPassword());
                        if(pw_field_entry.length() > 16) {
                            password.setBackground(Color.RED);
                            error_found++;
                        }
                        else {
                            password.setBackground(Color.WHITE);
                        }

                        if (error_found == 0) {
                            GROUP_PORT = Integer.valueOf(port_field_entry);
                        } else {
                            return;
                        }
 

                        calendar = new GregorianCalendar();
                        textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Attempting to connect to " + GROUP_IP + ":" + GROUP_PORT + ".\n");
                        textArea1.setCaretPosition(textArea1.getText().length());


                        try {
                            socket_to_group = new DatagramSocket();
                        } catch (SocketException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, could not connect to the requested server.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());

                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }

                            textField.requestFocus();
                            menu_connect.setEnabled(true);
                            setEnabled(true);
                            connect.dispose();

                            return;
                        }

                        byte[] buf = new byte[1 + 17 + 17 + 2];
                        
                        buf[0] = new Integer(0x0D).byteValue();

                        byte[] password_bytes = pw_field_entry.getBytes();

                        int i, j;
                        for (i = 1, j = 0; j < password_bytes.length && i < 17; i++, j++) {
                            buf[i] = password_bytes[j];
                        }
                        buf[i] = new Integer(0x00).byteValue();

                        byte[] name_bytes = name_field_entry.getBytes();


                        for (i = 18, j = 0; j < name_bytes.length && i < 34; i++, j++) {
                            buf[i] = name_bytes[j];
                        }
                        buf[i] = new Integer(0x00).byteValue();

                        RECIEVING_PORT = 0;
                        try {
                            ServerSocket test = new ServerSocket(0);
                            RECIEVING_PORT = test.getLocalPort();
                            test.close();
                        } catch (IOException ex) {}

                        try {
                            socket_from_group = new DatagramSocket(RECIEVING_PORT);
                        } catch (SocketException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, not enough free ports to listen.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());

                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }

                            textField.requestFocus();
                            menu_connect.setEnabled(true);
                            setEnabled(true);
                            connect.dispose();
                            socket_to_group.close();
                            return;
                        }

                        buf[35] = (byte) (RECIEVING_PORT >>> 8);
                        buf[36] = (byte) (RECIEVING_PORT);

                        DatagramPacket packet = null;
                        try {
                            packet = new DatagramPacket(buf, buf.length, getAddress(GROUP_IP), GROUP_PORT);
                        } 
                        catch (UnknownHostException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, could not connect to the requested group.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());

                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }

                            textField.requestFocus();
                            menu_connect.setEnabled(true);
                            setEnabled(true);
                            connect.dispose();
                            socket_to_group.close();
                            return;
                        }
                        
                        try {
                            reliable_send(socket_to_group, packet);
                        } catch (IOException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, could not connect to the requested server.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());

                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }

                            textField.requestFocus();
                            menu_connect.setEnabled(true);
                            setEnabled(true);
                            connect.dispose();
                            socket_to_group.close();

                            return;
                        }

                        packet = new DatagramPacket(buf, buf.length);
                        try {
                           packet = receive(socket_to_group, packet);
                           buf = packet.getData();
                        } catch (SocketTimeoutException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, could not connect to the requested server.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());

                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }

                            textField.requestFocus();
                            menu_connect.setEnabled(true);
                            setEnabled(true);
                            connect.dispose();
                            socket_to_group.close();

                            return;
                        } catch (IOException ex) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, could not connect to the requested server.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());

                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }

                            textField.requestFocus();
                            menu_connect.setEnabled(true);
                            setEnabled(true);
                            connect.dispose();
                            socket_to_group.close();

                            return;
                        }

                        if (new Byte(buf[0]).intValue() == 0x0E) {
                            if (new Byte(buf[1]).intValue() == 0x00) {
                                for (i = 0; buf[i + 2] != 0x00; i++) {
                                }

                                byte[] group_name_bytes = new byte[i];

                                for (i = 0; i < group_name_bytes.length; i++) {
                                    group_name_bytes[i] = buf[i + 2];
                                }

                                group_name = new String(group_name_bytes);
                                
                                group_message_counter = ((buf[19] & 0xFF) << 24) + ((buf[20] & 0xFF) << 16) + ((buf[21] & 0xFF) << 8) + (buf[22] & 0xFF);
   
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "You have joined the group, " + group_name + ".\n");
                                textArea1.setCaretPosition(textArea1.getText().length());
                            } else if (new Byte(buf[1]).intValue() == 0x01) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, the name already exists on the group.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());

                                if (enabled_sounds[5]) {
                                    playSound(selected_sounds[5]);
                                }

                                textField.requestFocus();
                                menu_connect.setEnabled(true);
                                setEnabled(true);
                                connect.dispose();
                                socket_to_group.close();
                                return;
                            } else if (new Byte(buf[1]).intValue() == 0x02) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, the group is full.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());

                                if (enabled_sounds[5]) {
                                    playSound(selected_sounds[5]);
                                }

                                textField.requestFocus();
                                menu_connect.setEnabled(true);
                                setEnabled(true);
                                connect.dispose();
                                socket_to_group.close();
                                return;
                            } else if (new Byte(buf[1]).intValue() == 0x03) {
                                calendar = new GregorianCalendar();
                                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, the password is incorrect.\n");
                                textArea1.setCaretPosition(textArea1.getText().length());

                                if (enabled_sounds[5]) {
                                    playSound(selected_sounds[5]);
                                }

                                textField.requestFocus();
                                menu_connect.setEnabled(true);
                                setEnabled(true);
                                connect.dispose();
                                socket_to_group.close();
                                return;
                            }
                        }
                        else {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "Connection Failed, could not connect to the requested server.\n");
                            textArea1.setCaretPosition(textArea1.getText().length());

                            if (enabled_sounds[5]) {
                                playSound(selected_sounds[5]);
                            }

                            textField.requestFocus();
                            menu_connect.setEnabled(true);
                            setEnabled(true);
                            connect.dispose();
                            socket_to_group.close();
                            return;
                        }

                        state = StateOfClient.CONNECTED;

                        setTitle("Chat Client BETA" + " [ " + name_field_entry +   " @ " + group_name + " ]");
                        textField.requestFocus();
                        menu_disconnect.setEnabled(true);

                        if (enabled_sounds[0]) {
                            playSound(selected_sounds[0]);
                        }

                        settings.setNameFieldEntry(name_field_entry);
                        settings.setIpFieldEntry(ip_field_entry);
                        settings.setPortEntry(port_field_entry);
                        saveSettings();

                        setEnabled(true);
                        connect.dispose();
                        
                    }
                });

                JButton close = new JButton("Cancel");
                close.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        if (name_field_entry.length() < 1 || name_field_entry.length() > MAX_SIZE_OF_NAME) {
                            name_field_entry = "";
                        }

                        
                        try {
                            if (Integer.parseInt(port_field_entry) < 0 || Integer.parseInt(port_field_entry) > 65535) {
                                port_field_entry = "";
                            }
                        } catch (NumberFormatException ex) {
                            port_field_entry = "";
                        }

                        settings.setNameFieldEntry(name_field_entry);
                        settings.setIpFieldEntry(ip_field_entry);
                        settings.setPortEntry(port_field_entry);
                        saveSettings();

                        textField.requestFocus();
                        menu_connect.setEnabled(true);
                        setEnabled(true);
                        connect.dispose();

                    }
                });

                JPanel panel4 = new JPanel();
                JPanel panel5 = new JPanel();
                JPanel panel6 = new JPanel();
                JPanel panel7 = new JPanel();

                panel4.setBackground(Color.LIGHT_GRAY);
                panel5.setBackground(Color.LIGHT_GRAY);
                panel6.setBackground(Color.LIGHT_GRAY);
                panel7.setBackground(Color.LIGHT_GRAY);
                panel4.add(new JLabel("User Name"));
                panel4.add(name_field);
                panel4.add(new JLabel("        "));
                panel5.add(new JLabel("IP Address"));
                panel5.add(ip_field);
                panel5.add(new JLabel("        "));
                panel6.add(new JLabel("         "));
                panel6.add(new JLabel("Port"));
                panel6.add(port_field);
                panel6.add(new JLabel("                                     "));
                panel7.add(new JLabel(""));
                panel7.add(new JLabel("Password"));
                panel7.add(password);
                panel7.add(new JLabel("        "));
                
                connect.add(new JLabel("               "));
                connect.add(panel4);
                connect.add(panel5);
                connect.add(panel6);
                connect.add(panel7);
                connect.add(connectButton);
                connect.add(close);
                connect.setVisible(true);

            }
        });

        menu_disconnect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                disconnect(0);
                
            }
        });



        addWindowListener(new WindowAdapter() {

            public void windowOpened(WindowEvent e) {
                textField.requestFocus();
            }
            
        });

        menu_about.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int about_window_width;
                int about_window_height;
                
                if(System.getProperty("os.name").equals("Windows XP") || System.getProperty("os.name").equals("Windows 7")) {
                    about_window_width = 220;
                    about_window_height = 200;
                }
                else {
                    about_window_width = 295;
                    about_window_height = 255;
                }

                menu_about.setEnabled(false);
                about = new JFrame("About");
                about.setSize(about_window_width, about_window_height);
                about.setLocation((int) (getLocation().getX() + getSize().getWidth() / 2) - (about_window_width / 2), (int) (getLocation().getY() + getSize().getHeight() / 2) - (about_window_height / 2));
                about.getContentPane().setBackground(background_color);
                about.setResizable(false);
                setEnabled(false);

                about.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        menu_about.setEnabled(true);
                        textField.requestFocus();
                        setEnabled(true);
                        about.dispose();
                    }
                });

                about.setLayout(new FlowLayout());
                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(8, 1));
                panel.add(new JLabel(" "));
                panel.add(new JLabel("            Chat Client, beta version."));
                panel.add(new JLabel());
                panel.add(new JLabel("  Made by Chris Kalonakis using java  "));
                panel.add(new JLabel("  on NetBeans IDE 7.0.1"));
                panel.add(new JLabel());
                panel.add(new JLabel("       Contact: hrkalona@inf.uth.gr"));
                panel.add(new JLabel(" "));

                JButton close = new JButton("Close");
                close.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        menu_about.setEnabled(true);
                        textField.requestFocus();
                        setEnabled(true);
                        about.dispose();
                    }
                });

                about.add(new JLabel("                                                                                                          "));
                about.add(new JLabel("  "));
                about.add(panel);
                about.add(new JLabel("  "));
                about.add(close);
                about.setVisible(true);
            }
        });

    }

    private void disconnect(int mode) {

        try {
            socket_from_group.close();
            state = StateOfClient.DISCONNECTED;

            byte[] buf = new byte[1 + 17];
            
            buf[0] = new Integer(0x13).byteValue();

            byte[] name_bytes = name_field_entry.getBytes();

            int j;
            for (i = 1, j = 0; j < name_bytes.length && j < 17; i++, j++) {
                buf[i] = name_bytes[j];
            }
            buf[i] = new Integer(0x00).byteValue();


            DatagramPacket packet = new DatagramPacket(buf, buf.length, getAddress(GROUP_IP), GROUP_PORT);
            try {
                reliable_send(socket_to_group, packet);
            } 
            catch (IOException ex) {}


            socket_to_group.close();
            menu_disconnect.setEnabled(false);
            menu_connect.setEnabled(true);
            if(mode == 0) {
                calendar = new GregorianCalendar();  
                textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "You have left the group, " + group_name + ".\n");
                textArea1.setCaretPosition(textArea1.getText().length());
                textArea2.setText("");
                setTitle("Chat Client BETA");
                
                message_counter = 0;
                group_message_counter = -1;

                if(enabled_sounds[1]) {
                    playSound(selected_sounds[1]);
                }
            }

        } 
        catch (Exception ex) {}
        textField.requestFocus();

    }
    
    private void serverTermination() {
        
        socket_from_group.close();
        state = StateOfClient.DISCONNECTED;
        
        socket_to_group.close();
        menu_disconnect.setEnabled(false);
        menu_connect.setEnabled(true);
        
        calendar = new GregorianCalendar();  
        textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "You have left the group, " + group_name + " (The server is shutting down).\n");
        textArea1.setCaretPosition(textArea1.getText().length());
        textArea2.setText("");
        setTitle("Chat Client BETA");
        
        message_counter = 0;
        group_message_counter = -1;

        if(enabled_sounds[1]) {
            playSound(selected_sounds[1]);
        }
        
        textField.requestFocus();
        
    }
    
    private void serverTimedOut() {
        
        socket_from_group.close();
        state = StateOfClient.DISCONNECTED;
        
        socket_to_group.close();
        menu_disconnect.setEnabled(false);
        menu_connect.setEnabled(true);
        
        calendar = new GregorianCalendar();  
        textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + "You have left the group, " + group_name + " (The server timed out).\n");
        textArea1.setCaretPosition(textArea1.getText().length());
        textArea2.setText("");
        setTitle("Chat Client BETA");
        
        message_counter = 0;
        group_message_counter = -1;

        if(enabled_sounds[1]) {
            playSound(selected_sounds[1]);
        }
        
        textField.requestFocus();
        
    }

    private InetAddress getAddress(String IP) throws UnknownHostException {

        InetAddress address = null;

        address = InetAddress.getByName(IP);
  
        return address;

    }

    public void run() {

        while (running) {

            waitForMessages();

        }

    }
    
    private synchronized void reliable_send(DatagramSocket socket, DatagramPacket packet) throws IOException {
      byte[] buf = new byte[1];
      boolean waiting = true;
      int timeouts = 0;
      
        byte[] buf2 = new byte[packet.getData().length + 4]; //header
        buf2[0] = (byte)(message_counter >>> 24);
        buf2[1] = (byte)(message_counter >>> 16);
        buf2[2] = (byte)(message_counter >>> 8);
        buf2[3] = (byte)(message_counter);
      
        for(int k = 0; k < packet.getData().length; k++) {
            buf2[k + 4] = packet.getData()[k];
        }
        
        packet.setData(buf2);
                    
        do {
            
            try {
                socket.send(packet);
                packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(300);
                socket.receive(packet);
                if(new Byte(buf[0]).intValue() == 0x00) {
                    waiting = false;   
                }
            } 
            catch(SocketTimeoutException ex) {}
            catch(NullPointerException ex) {}
            catch(IOException ex) {
                if(state == StateOfClient.CONNECTED) {
                    serverTimedOut();
                    return;
                }
                else {
                    throw new IOException();
                }
            }
            
            timeouts++;
            
        } while(waiting && timeouts < MAX_TIMEOUTS);
        
        if(timeouts == MAX_TIMEOUTS) {
            if(state == StateOfClient.CONNECTED) {
                serverTimedOut();
                return;
            }
            else {
                throw new IOException();
            }
        }
        
        message_counter++;
               
    }
    
    private DatagramPacket receive(DatagramSocket socket, DatagramPacket packet) throws SocketTimeoutException, IOException {
        
        
        packet.setData(new byte[packet.getData().length + 4]);

        if(state == StateOfClient.CONNECTED) {
            socket.setSoTimeout(300000); //5 min
        }
        else {
            socket.setSoTimeout(4000); //4 secs
        }
        socket.receive(packet);
                    
        int group_message_counter2 = ((packet.getData()[0] & 0xFF) << 24) + ((packet.getData()[1] & 0xFF) << 16) + ((packet.getData()[2] & 0xFF) << 8) + (packet.getData()[3] & 0xFF);
        
        if(group_message_counter > -1) {
            if(group_message_counter2 > group_message_counter) { //new message
                byte[] buf = new byte[1];
                buf[0] = new Integer(0x00).byteValue();
                DatagramPacket packet2 = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                socket.send(packet2);
                
                byte[] buf2 = new byte[packet.getData().length - 4];
                
                for(int j = 0; j < buf2.length; j++) {
                    buf2[j] = packet.getData()[j + 4];
                }
        
                packet.setData(buf2);
                
                group_message_counter++;
            }
            else { //old  
                byte[] buf = new byte[1];
                buf[0] = new Integer(0x00).byteValue();
                DatagramPacket packet2 = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                socket.send(packet2);
                
                byte[] buf2 = new byte[packet.getData().length - 4];
                
                for(int j = 0; j < buf2.length; j++) {
                    buf2[j] = packet.getData()[j + 4];
                }
        
                buf2[0] = new Integer(0x30).byteValue(); //No type value 
                
                packet.setData(buf2);                   
            }
        }
        else {  //first message
            byte[] buf = new byte[1];
            buf[0] = new Integer(0x00).byteValue();
            DatagramPacket packet2 = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
            socket.send(packet2);
                
            byte[] buf2 = new byte[packet.getData().length - 4];
                
            for(int j = 0; j < buf2.length; j++) {
                buf2[j] = packet.getData()[j + 4];
            }
        
            packet.setData(buf2);
        }
        
        return packet;
              
    }
    
    private void waitForMessages() {
        String name;
        byte[] name_bytes;
        byte[] buf = new byte[3 + 17 * 100];



        while (state == StateOfClient.CONNECTED) {

            DatagramPacket packet = new DatagramPacket(buf, buf.length);


            try {

                packet = receive(socket_from_group, packet);
                buf = packet.getData();
                
                switch (new Byte(buf[0]).intValue()) {
                    case 0x0E: //user joined
                        for (i = 0; buf[i + 1] != 0x00; i++) {
                        }

                        name_bytes = new byte[i];

                        for (i = 0; i < name_bytes.length; i++) {
                            name_bytes[i] = buf[i + 1];
                        }

                        name = new String(name_bytes);

                        calendar = new GregorianCalendar();
                        textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + name + " has joined the group.\n");
                        textArea1.setCaretPosition(textArea1.getText().length());

                        if (enabled_sounds[2]) {
                            playSound(selected_sounds[2]);
                        }

                        break;
                    case 0x10: //message
                        for (i = 0; buf[i + 1] != 0x00; i++) {
                        }

                        name_bytes = new byte[i];

                        for (i = 0; i < name_bytes.length; i++) {
                            name_bytes[i] = buf[i + 1];
                        }

                        name = new String(name_bytes);

                        for (i = 0; buf[i + 18] != 0x00; i++) {
                        }

                        byte[] message_bytes = new byte[i];

                        for (i = 0; i < message_bytes.length; i++) {
                            message_bytes[i] = buf[i + 18];
                        }

                        String message = new String(message_bytes);


                        calendar = new GregorianCalendar();
                        textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + name + " : " + message + "\n");
                        textArea1.setCaretPosition(textArea1.getText().length());

                        if (!name.equals(name_field_entry) && enabled_sounds[4]) {
                            playSound(selected_sounds[4]);
                        }

                        break;
                    case 0x12: //refresh list
                        textArea2.setText("");
                        int count = ((buf[1] & 0xFF) << 8) + (buf[2] & 0xFF);

                        for (int k = 0; k < count; k++) {
                            for (i = 0; buf[i + 1 + 2 + k * 17] != 0x00; i++) {
                            }
                            name_bytes = new byte[i];

                            for (i = 0; i < name_bytes.length; i++) {
                                name_bytes[i] = buf[i + 1 + 2 + k * 17];
                            }
                            textArea2.setText(textArea2.getText() + new String(name_bytes) + "\n");
                        }
                        break;
                    case 0x14: //user left
                        for (i = 0; buf[i + 2] != 0x00; i++) {
                        }

                        name_bytes = new byte[i];

                        for (i = 0; i < name_bytes.length; i++) {
                            name_bytes[i] = buf[i + 2];
                        }

                        name = new String(name_bytes);

                        if(new Byte(buf[1]).intValue() == 0x00) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + name + " has left the group (Disconnect by user).\n");
                            textArea1.setCaretPosition(textArea1.getText().length());  
                        }
                        else if(new Byte(buf[1]).intValue() == 0x01) {
                            calendar = new GregorianCalendar();
                            textArea1.setText(textArea1.getText() + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "> " + name + " has left the group (User timed out).\n");
                            textArea1.setCaretPosition(textArea1.getText().length());  
                        }
                        

                        if (enabled_sounds[3]) {
                            playSound(selected_sounds[3]);
                        }
                        break;
                    case 0x15: //server terminating
                        serverTermination();
                        break;
                    default:
                        break;
                }
            } 
            catch (SocketTimeoutException ex) {
                searchRecord("ChatServerUDP");  //update main server info just in case
            } 
            catch (IOException ex) {}
        }
      

    }

    private void saveSettings() {

        ObjectOutputStream file = null;

        try {
            file = new ObjectOutputStream(new FileOutputStream("settings.dat"));
            file.writeObject(settings);
            file.flush();
        } 
        catch (IOException ex) {}

        try {
            file.close();
        } catch (Exception ex) {}

    }

    private void loadSettings() {

        ObjectInputStream file = null;
        try {
            file = new ObjectInputStream(new FileInputStream("settings.dat"));
            settings = (Settings) file.readObject();
        } catch (IOException ex) {
            settings = new Settings();
            saveSettings();
        } catch (ClassNotFoundException ex) {
            settings = new Settings();
            saveSettings();
        }

        try {
            file.close();
        } 
        catch (Exception ex) {}

    }

    private void playSound(String sound) {

        InputStream sound_stream_in = null;
        try {
            URL url = getClass().getResource("/Sounds/" + sound + ".wav");
            sound_stream_in = url.openStream();
        } 
        catch (FileNotFoundException ex)  {}
        catch(IOException ex) {}

        AudioStream as = null;
        try {
            as = new AudioStream(sound_stream_in);
        } 
        catch (IOException ex) {}

        try {
            AudioPlayer.player.start(as);
        }
        catch(Exception ex) {}

    }

    private void lastUsedListInsertion() {

        if (last_used.size() == NUMBER_OF_LAST_USED) {
            last_used.remove(0);
            last_used.add(new String(chat_entry));
            last_used_index = last_used.size() - 1;
        } else {
            last_used.add(new String(chat_entry));
            last_used_index = last_used.size() - 1;
        }

    }
    
    @Override
    public void addRecord(String name, String IP, int PORT, String properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeRecord(String name, String IP, int PORT) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void searchRecord(String name) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } 
        catch (SocketException ex) {
            //System.out.println("\nA problem while handling the socket has been occured.");
            return;
        }
              
 
        // send request
        byte[] buf = new byte[3 + 727 * 90];
        
        buf[0] = new Integer(0x05).byteValue();
        
        byte[] name_bytes = name.getBytes();
        
        int i, j;
        for(i = 1, j = 0; j < name_bytes.length && i < 64; i++, j++) {
            buf[i] = name_bytes[j];
        }
        buf[i] = new Integer(0x00).byteValue();
        
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(buf, buf.length, getAddress(DIRECTORY_SERVER_IP), DIRECTORY_SERVER_PORT);
        } 
        catch (UnknownHostException ex) {}
        
        try {
            socket.send(packet);
        } 
        catch (IOException ex) {
            //System.out.println("\nA problem while sending the packet has been occured.");
            return;
        }
     
        // get response
        packet = new DatagramPacket(buf, buf.length);
        try {
            socket.setSoTimeout(4000);
            socket.receive(packet);
        }
        catch(SocketTimeoutException ex) {
            //System.out.println("\n4 seconds passed, without any response from the server. Terminating.");
            return;
        }
        catch (IOException ex) {
            //System.out.println("\nA problem while recieving the response has been occured.");
            return;
        }
        
        if(new Byte(buf[0]).intValue() == 0x06) {      
            int count = ((buf[1]  & 0xFF) << 8) + (buf[2] & 0xFF);
            
            if(count == 0) {
                //System.out.println("No records found, starting with " + name + " as a prefix.");
            }
            else {
                
                //System.out.println(count + " records found, starting with " + name + " as a prefix.\n");
                
                for(int k = 0; k < count; k++) {
                    
                    for(i = 0; buf[i + 1 + 2 + k * 90] != 0x00; i++) {}
                    name_bytes = new byte[i];
        
                    for(i = 0; i < name_bytes.length; i++) {
                        name_bytes[i] = buf[i + 1 + 2 + k * 90];   
                    }
                    String name2 = new String(name_bytes);
                    
                    for(i = 0; buf[i + 65 + 2 + k * 90] != 0x00; i++) {}
        
                    byte[] ip_bytes = new byte[i];
        
                    for(i = 0; i < ip_bytes.length; i++) {
                        ip_bytes[i] = buf[i + 65 + 2 + k * 90];
                    }
                    String IP = new String(ip_bytes);
                                     
                    int PORT = ((buf[81 + 2 + k * 90] & 0xFF) << 8) + (buf[82 + 2 + k * 90] & 0xFF);           
        
                    for(i = 0; buf[i + 83 + 2 + k * 90] != 0x00; i++) {}
        
                    byte[] properties_bytes = new byte[i];
        
                    for(i = 0; i < properties_bytes.length; i++) {
                        properties_bytes[i] = buf[i + 83 + 2 + k * 90];
                    }
                    String properties = new String(properties_bytes);
                    
                    //System.out.println("Record " + (k + 1) + ",\n      Name: " + name2 + "\n        IP: " + IP + "\n      PORT: " + PORT + "\nProperties: " + properties + "\n");
                    
                    if(name2.equals(name)) {
                        SERVER_IP = IP;
                        SERVER_PORT = PORT;
                    }
                }
            }
            
        }
        else {
            //System.out.println("\nWrong packet received.");
        }
     
        socket.close();
    }

    public static void main(String[] args) throws IOException {

        ChatClient client = new ChatClient();
        client.setVisible(true);
        client.run();

    }
   
}
