package io.github.rmuhamedgaliev.arcana.core.player;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private final Map<String, Object> attributes;

    public Player() {
        this.attributes = new HashMap<>();
    }

    // Установить значение атрибута
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    // Получить значение атрибута (с значением по умолчанию, если атрибут отсутствует)
    public Object getAttribute(String key, Object defaultValue) {
        return attributes.getOrDefault(key, defaultValue);
    }

    // Получить значение атрибута как число (если это возможно)
    public int getAttributeAsInt(String key, int defaultValue) {
        Object value = attributes.get(key);
        if (value instanceof Integer) {
            return (int) value;
        }
        return defaultValue;
    }

    // Проверить наличие атрибута
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    // Увеличить значение атрибута (если это число)
    public void increaseAttribute(String key, int amount) {
        int currentValue = getAttributeAsInt(key, 0);
        setAttribute(key, currentValue + amount);
    }

    // Уменьшить значение атрибута (если это число)
    public void decreaseAttribute(String key, int amount) {
        int currentValue = getAttributeAsInt(key, 0);
        setAttribute(key, currentValue - amount);
    }

    // Получить все атрибуты (для отладки или сохранения состояния)
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
}
