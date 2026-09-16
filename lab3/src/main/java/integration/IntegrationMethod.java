package integration;

public enum IntegrationMethod {
    LEFT_RECTANGLES("Метод левых прямоугольников", 1),
    RIGHT_RECTANGLES("Метод правых прямоугольников", 1),
    MIDDLE_RECTANGLES("Метод средних прямоугольников", 2),
    TRAPEZOID("Метод трапеций", 2),
    SIMPSON("Метод Симпсона", 4),
    MONTE_CARLO("Метод Монте-Карло", 0);

    private final String name;
    private final int k;

    IntegrationMethod(String name, int k) {
        this.name = name;
        this.k = k;
    }

    public String getName() {
        return name;
    }

    public int getK() {
        return k;
    }
}
