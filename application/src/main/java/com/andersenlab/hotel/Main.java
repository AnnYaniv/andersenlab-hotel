package com.andersenlab.hotel;

import com.andersenlab.hotel.common.reader.PropertyReaderFromFile;
import com.andersenlab.hotel.common.service.ContextBuilder;
import com.andersenlab.hotel.http.ServletStarter;

public class Main {

    public static void main(String[] args) {
        final HotelModule context = initContext();
        getStarter(context).run();
    }

    public static ServletStarter getStarter(HotelModule context) {
        return ServletStarter.forModule(context);
    }

    public static HotelModule initContext() {
        PropertyReaderFromFile propertyReaderFromFile = new PropertyReaderFromFile("application.properties");
        String abilityApartmentToChange = propertyReaderFromFile.readProperty("apartment.change.enabled");
        final String persistence = "JPA_persistence";

        return new ContextBuilder().initJpa(persistence)
                .doRepositoryThreadSafe()
                .initServices()
                .initCheckInCheckOut(Boolean.parseBoolean(abilityApartmentToChange))
                .build();
    }
}