package com.auroratms.tournamentevententry.pricecalc;

import java.util.Date;

public interface IPriceCalculator {

    double calculatePrice (Date dateOfEntry, Date birthDate);
}
