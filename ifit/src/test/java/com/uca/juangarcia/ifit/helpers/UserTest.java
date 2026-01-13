package com.uca.juangarcia.ifit.helpers;

import java.util.Random;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;
import com.uca.juangarcia.ifit.modules.user.model.AppRole;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;

public class UserTest {

    static public AppUser createUserTest(int seed) {
        Faker faker = new Faker(new Random(seed));
        Name name = faker.name();

        AppRole appRole = new AppRole("user");

        CoachModelType coachModelType = new CoachModelType("test",
                null,
                null,
                true,
                null,
                null);

        AppUser user = new AppUser();

        System.out.println("Created " + user.toString());
        return user;
    }
}
