package main.java.Mercury.model;

public enum Company {
    BAGIRA_CHAIKA("Багира-Чайка", 2),
    BAGIRA_KLAMAS("Багира-Кламас", 3),
    BAGIRA_OG("Багира-ОГ", 4),
    BAGIRA_MERCURY("Багира-Меркурий", 5),
    BAGIRA_PERVOM("Багира-Первом", 6),
    BAGIRA_PUSHKINA("Багира-Пушкина", 7);

    private String name;
    private int count = -1;
    private int numberOfRadioButton;


    public String getName() {
        if (count == -1) {
            return name + " (...)";
        } else {
            return name + " (" + count + ")";
        }
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public int getNumberOfRadioButton() {
        return numberOfRadioButton;
    }

    Company(String name, int numberOfRadioButton) {
        this.name = name;
        this.numberOfRadioButton = numberOfRadioButton;
    }
}
