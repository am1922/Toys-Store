import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

interface Toy {
    int getId();

    String getName();

    int getQuantity();

    double getWeight();

    void setWeight(double weight);

    void decreaseQuantity();

    String toString();
}

class ToyImpl implements Toy {
    private int id;
    private String name;
    private int quantity;
    private double weight;

    public ToyImpl(int id, String name, int quantity, double weight) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.weight = weight;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public void decreaseQuantity() {
        quantity--;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Quantity: " + quantity;
    }
}

interface ToyStore {
    void addToy(Toy toy);

    void updateWeight(int toyId, double newWeight);

    void organizeGiveaway();

    void displayWonToys();
}

class FileToyStore implements ToyStore {
    private List<Toy> availableToys;
    private List<Toy> wonToys;
    private String lastSessionTime;
    private Set<Integer> wonToyIds;

    public FileToyStore() {
        this.availableToys = new ArrayList<>();
        this.wonToys = new ArrayList<>();
        this.lastSessionTime = "";
        this.wonToyIds = new HashSet<>();
        loadAvailableToysFromFile("available_toys.txt");
        loadLastSessionTimeFromFile("last_session_time.txt");
        loadWonToyIdsFromFile("won_toy_ids.txt");
    }

    private void loadAvailableToysFromFile(String filename) {
        try {
            List<String> lines = Files.readAllLines(Path.of(filename));
            for (String line : lines) {
                String[] parts = line.split(", ");
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                double weight = Double.parseDouble(parts[3]);
                availableToys.add(new ToyImpl(id, name, quantity, weight));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAvailableToysToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Toy toy : availableToys) {
                writer.write(toy.getId() + ", " + toy.getName() + ", " + toy.getQuantity() + ", " + toy.getWeight() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWonToyIdsFromFile(String filename) {
        try {
            List<String> lines = Files.readAllLines(Path.of(filename));
            for (String line : lines) {
                int id = Integer.parseInt(line.trim());
                wonToyIds.add(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasToyBeenWon(int toyId) {
        return wonToyIds.contains(toyId);
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void loadLastSessionTimeFromFile(String filename) {
        try {
            List<String> lines = Files.readAllLines(Path.of(filename));
            if (!lines.isEmpty()) {
                lastSessionTime = lines.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveLastSessionTimeToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(lastSessionTime + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToy(Toy toy) {
        availableToys.add(toy);
        saveAvailableToysToFile("available_toys.txt");
    }

    public void updateWeight(int toyId, double newWeight) {
        for (Toy toy : availableToys) {
            if (toy.getId() == toyId) {
                toy.setWeight(newWeight);
                saveAvailableToysToFile("available_toys.txt");
                System.out.println("Вес игрушки с ID " + toyId + " обновлен.");
                return;
            }
        }
        System.out.println("Игрушка с ID " + toyId + " не найдена.");
    }

    public void organizeGiveaway() {
        if (availableToys.isEmpty()) {
            System.out.println("Розыгрыш окончен. Все игрушки разыграны.");
            saveLastSessionTimeToFile("last_session_time.txt");
            System.exit(0);
        }

        List<Toy> giveawayToys = new ArrayList<>();

        for (Toy toy : availableToys) {
            if (!hasToyBeenWon(toy.getId())) {
                int numToGiveaway = (int) ((toy.getWeight() / 100) * 10);
                for (int i = 0; i < numToGiveaway; i++) {
                    giveawayToys.add(toy);
                }
            }
        }

        if (giveawayToys.isEmpty()) {
            System.out.println("Нет игрушек для разыгрыша.");
            return;
        }

        Random random = new Random();
        int index = random.nextInt(giveawayToys.size());
        Toy prizeToy = giveawayToys.get(index);

        wonToys.add(prizeToy);

        for (Iterator<Toy> iterator = availableToys.iterator(); iterator.hasNext();) {
            Toy availableToy = iterator.next();
            if (availableToy.getId() == prizeToy.getId()) {
                availableToy.decreaseQuantity();
                if (availableToy.getQuantity() == 0) {
                    iterator.remove();
                }
                break;
            }
        }

        lastSessionTime = getCurrentTime();

        saveWonToysToFile("won_toys.txt", prizeToy);
        saveAvailableToysToFile("available_toys.txt");
        saveLastSessionTimeToFile("last_session_time.txt");

        System.out.println("Поздравляем! Вы выиграли: " + prizeToy.getName());
    }

    private void saveWonToysToFile(String filename, Toy prizeToy) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(prizeToy.getId() + ", " + prizeToy.getName() + ", " + getCurrentTime() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayWonToys() {
        System.out.println("Выигранные игрушки:");
        for (Toy toy : wonToys) {
            System.out.println(toy);
        }
    }

    public static void main(String[] args) {
        FileToyStore toyStore = new FileToyStore();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Выберите действие:");
            System.out.println("1. Добавить игрушку");
            System.out.println("2. Обновить вес игрушки");
            System.out.println("3. Розыгрыш игрушек");
            System.out.println("4. Показать выигранные игрушки");
            System.out.println("5. Выйти из программы");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Введите ID игрушки:");
                    int id = scanner.nextInt();
                    System.out.println("Введите название игрушки:");
                    scanner.nextLine(); // Очистка буфера
                    String name = scanner.nextLine();
                    System.out.println("Введите количество игрушек:");
                    int quantity = scanner.nextInt();
                    System.out.println("Введите вес игрушки (% от общего веса):");
                    double weight = scanner.nextDouble();

                    toyStore.addToy(new ToyImpl(id, name, quantity, weight));
                    break;
                case 2:
                    System.out.println("Введите ID игрушки, у которой нужно обновить вес:");
                    int toyId = scanner.nextInt();
                    System.out.println("Введите новый вес игрушки:");
                    double newWeight = scanner.nextDouble();

                    toyStore.updateWeight(toyId, newWeight);
                    break;
                case 3:
                    toyStore.organizeGiveaway();
                    break;
                case 4:
                    toyStore.displayWonToys();
                    break;
                case 5:
                    System.out.println("Программа завершена.");
                    scanner.close();
                    System.exit(0);
            }
        }
    }
}
