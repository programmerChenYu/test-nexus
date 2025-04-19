package io.github.programmerchenyu.enums.chain;

/**
 * @author 爱吃小鱼的橙子
 */
public enum ProcessorEnum {

    CONFIGURATION_CLASS(0),
    BEAN_METHODS(1),
    END(2);

    private final Integer index;

    ProcessorEnum(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return this.index;
    }
}
