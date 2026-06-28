import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class KalkulatorProduktow extends JFrame {

    private final String[] produkty = {
            "Laptop", "Mysz", "Klawiatura", "Monitor", "Słuchawki"
    };
    private final double[] ceny = {3499.99, 89.99, 149.99, 899.99, 199.99};

    // Lista rozwijana (JComboBox) do wyboru zamawianego produktu
    private final JComboBox<String> listaProduktow = new JComboBox<>(produkty);
    // Model numeryczny JSpinner określający minimalną, maksymalną oraz domyślną ilość
    private final SpinnerNumberModel modelIlosci = new SpinnerNumberModel(1, 1, 99, 1);
    // Komponent JSpinner (pokrętło numeryczne) do precyzyjnego wyboru liczby sztuk
    private final JSpinner spinnerIlosci = new JSpinner(modelIlosci);
    // Obszar tekstowy prezentujący podsumowanie pozycji w bieżącym koszyku

    private final JTextArea obszarZamowienia = new JTextArea(10, 40);
    // Etykieta wyświetlająca sumaryczną kwotę do zapłaty
    private final JLabel etykietaSumy = new JLabel("Łączna kwota: 0,00 zł");
    private final JButton przyciskDodaj = new JButton("Dodaj do zamówienia");
    private final JButton przyciskWyczysc = new JButton("Wyczyść");
    private final JButton przyciskZapisz = new JButton("Zapisz zamówienie");
    private final JButton przyciskWczytaj = new JButton("Wczytaj zamówienie");

    // Dynamiczna lista przechowująca poszczególne wiersze zamówienia
    private final ArrayList<String> pozycjeZamowienia = new ArrayList<>();
    // Zmienna przechowująca całkowity koszt wszystkich zamówionych produktów
    private double sumaCen = 0.0;
    // Nazwa pliku z zapisanym zamówieniem
    private static final String NAZWA_PLIKU = "zamowienie_produktow.txt";

    public KalkulatorProduktow() {
        setTitle("Kalkulator zamówienia");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 480);
        setLocationRelativeTo(null);

        obszarZamowienia.setEditable(false);
        obszarZamowienia.setLineWrap(true);

        JPanel panelGorny = new JPanel(new GridLayout(3, 2, 8, 8));
        panelGorny.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        panelGorny.add(new JLabel("Produkt:"));
        panelGorny.add(listaProduktow);
        panelGorny.add(new JLabel("Ilość:"));
        panelGorny.add(spinnerIlosci);
        panelGorny.add(przyciskDodaj);
        panelGorny.add(przyciskWyczysc);

        JPanel panelSrodkowy = new JPanel(new BorderLayout());
        panelSrodkowy.setBorder(BorderFactory.createTitledBorder("Zamówienie"));
        panelSrodkowy.add(new JScrollPane(obszarZamowienia), BorderLayout.CENTER);

        JPanel panelDolny = new JPanel(new BorderLayout());
        JPanel panelPrzyciskow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPrzyciskow.add(przyciskZapisz);
        panelPrzyciskow.add(przyciskWczytaj);
        panelDolny.add(etykietaSumy, BorderLayout.WEST);
        panelDolny.add(panelPrzyciskow, BorderLayout.EAST);
        panelDolny.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        setLayout(new BorderLayout());
        add(panelGorny, BorderLayout.NORTH);
        add(panelSrodkowy, BorderLayout.CENTER);
        add(panelDolny, BorderLayout.SOUTH);

        przyciskDodaj.addActionListener(e -> dodajDoZamowienia());
        przyciskWyczysc.addActionListener(e -> wyczyscZamowienie());
        przyciskZapisz.addActionListener(e -> zapiszZamowienie());
        przyciskWczytaj.addActionListener(e -> wczytajZamowienie());

    }

    // Dodaje wybraną ilość danego produktu do koszyka i aktualizuje kwotę
    private void dodajDoZamowienia() {
        int wybranyIndeks = listaProduktow.getSelectedIndex();
        String produkt = produkty[wybranyIndeks];
        int ilosc = (int) spinnerIlosci.getValue();
        double cena = ceny[wybranyIndeks];
        double wartoscPozycji = cena * ilosc;
        sumaCen += wartoscPozycji;

        String linia = String.format("%s x%d = %.2f zł", produkt, ilosc, wartoscPozycji);
        pozycjeZamowienia.add(linia);
        obszarZamowienia.append(linia + "\n");
        etykietaSumy.setText(String.format("Łączna kwota: %.2f zł", sumaCen));
        spinnerIlosci.setValue(1);
    }

    // Resetuje koszyk i zeruje całkowitą kwotę
    private void wyczyscZamowienie() {
        pozycjeZamowienia.clear();
        obszarZamowienia.setText("");
        sumaCen = 0.0;
        etykietaSumy.setText("Łączna kwota: 0,00 zł");
    }

    // Zapisuje wszystkie pozycje koszyka i sumę końcową do pliku
    private void zapiszZamowienie() {
        if (pozycjeZamowienia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Zamówienie jest puste!",
                    "Błąd", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (BufferedWriter zapisywacz = new BufferedWriter(new FileWriter(NAZWA_PLIKU))) {
            for (String linia : pozycjeZamowienia) {
                zapisywacz.write(linia);
                zapisywacz.newLine();
            }
            zapisywacz.write(String.format("SUMA: %.2f zł", sumaCen));
            JOptionPane.showMessageDialog(this, "Zamówienie zapisano do: " + NAZWA_PLIKU);
        } catch (IOException wyjatek) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + wyjatek.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Wczytuje pozycje z zapisanego zamówienia i odtwarza podsumowanie w oknie
    private void wczytajZamowienie() {
        wyczyscZamowienie();
        try (BufferedReader czytnik = new BufferedReader(new FileReader(NAZWA_PLIKU))) {
            String linia;
            StringBuilder buforTekstu = new StringBuilder();
            while ((linia = czytnik.readLine()) != null) {
                if (!linia.startsWith("SUMA:")) {
                    pozycjeZamowienia.add(linia);

                    buforTekstu.append(linia).append("\n");
                } else {
                    buforTekstu.append("\n").append(linia);
                    etykietaSumy.setText(linia.replace("SUMA:", "Łączna kwota:"));
                }
            }
            obszarZamowienia.setText(buforTekstu.toString());
        } catch (FileNotFoundException wyjatek) {
            JOptionPane.showMessageDialog(this, "Brak zapisanego zamówienia.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException wyjatek) {
            JOptionPane.showMessageDialog(this, "Błąd odczytu: " + wyjatek.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Zastąpiono SwingUtilities.invokeLater standardowym wywołaniem konstruktora i setVisible
        KalkulatorProduktow przyklad = new KalkulatorProduktow();
        przyklad.setVisible(true);
    }
}