package com.drillup.drillup;

public record OperationsModuleRecord(String id, String entity, Float amount) {

    @Override
    public String toString() {
        return id+"||"+entity+"||"+amount;
    }

    public String getId() {
        return id;
    }

    public String getEntity() {
        return entity;
    }

    public Float getAmount() {
        return amount;
    }


}
