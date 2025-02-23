package io.github.rmuhamedgaliev.arcana.core.quest;

public class EndCondition {
    private String type; // Тип условия (attribute, step)
    private String attribute; // Атрибут (если type = attribute)
    private int value; // Значение атрибута
    private String step; // Шаг (если type = step)
    private String message; // Сообщение при завершении

    // Геттеры и сеттеры
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
