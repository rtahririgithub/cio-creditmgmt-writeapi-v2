package com.telus.credit.model;
import java.math.BigDecimal;

// Used for the attachment size
public class Size {

    private BigDecimal amount;
    private String units;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

}
