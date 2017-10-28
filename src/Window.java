import javax.swing.*;

public class Window {

    public Window() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {

        }

        View view = new View();
        Model model = new Model();
        Controller controller = new Controller(view, model);
    }
}
