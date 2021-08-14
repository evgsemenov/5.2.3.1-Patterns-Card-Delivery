package ru.netology.delivery.test;

import com.epam.reportportal.junit5.ReportPortalExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Keys;
import ru.netology.delivery.data.DataGenerator;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

@ExtendWith(ReportPortalExtension.class)
class DeliveryTest {

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
        $("[data-test-id='date']").$("[class='input__control']").click();
        $("[data-test-id='date']").$("[class='input__control']").
                sendKeys(Keys.chord(Keys.CONTROL + "A", Keys.DELETE));
    }

    @AfterEach
    void tearDown() {
        close();
    }

    @Test
    @DisplayName("Should successful plan and replan meeting")
    void shouldSuccessfulPlanAndReplanMeeting() {
        var validUser = DataGenerator.Registration.generateUser("ru");
        var daysToAddForFirstMeeting = DataGenerator.generateRandomDateShift();
        var firstMeetingDate = DataGenerator.generateDate(daysToAddForFirstMeeting);
        var daysToAddForSecondMeeting = DataGenerator.generateRandomDateShift();
        var secondMeetingDate = DataGenerator.generateDate(daysToAddForSecondMeeting);
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(validUser.getCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(firstMeetingDate);
        $("[data-test-id= 'name']").$("[name ='name']").setValue(validUser.getName());
        $("[data-test-id='phone']").$("[name='phone']").setValue(validUser.getPhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='success-notification']").shouldBe(visible).
                shouldHave(exactText("Успешно!\n" + "Встреча успешно запланирована на " + firstMeetingDate));
        setup();
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(validUser.getCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(secondMeetingDate);
        $("[data-test-id= 'name']").$("[name ='name']").setValue(validUser.getName());
        $("[data-test-id='phone']").$("[name='phone']").setValue(validUser.getPhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id= 'replan-notification']").shouldHave(exactText("Необходимо подтверждение\n" +
                "У вас уже запланирована встреча на другую дату. Перепланировать?\n" + "Перепланировать"));
        $$("button").find(exactText("Перепланировать")).shouldBe(visible).click();
        $("[data-test-id='success-notification']").shouldBe(visible).
                shouldHave(exactText("Успешно!\n" + "Встреча успешно запланирована на " + secondMeetingDate));
    }

    @Test
    void shouldRequireValidCityIfCityNotIncludeTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateInvalidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(DataGenerator.
                generateDate(DataGenerator.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(DataGenerator.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(DataGenerator.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='city'].input_invalid .input__sub").shouldBe(visible).
                shouldHave(exactText("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldRequireValidDateIfTwoDaysTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateValidCity());
        $("[data-test-id='date']").$("[class='input__control']").setValue(DataGenerator.
                        generateDate(-1));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(DataGenerator.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(DataGenerator.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='date'] .input__sub").shouldBe(visible).
                shouldHave(exactText("Заказ на выбранную дату невозможен"));
    }

    @Test
    void shouldPlaceOrderIfFourDaysPositiveTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(DataGenerator.
                generateDate(4));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(DataGenerator.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(DataGenerator.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='success-notification']").shouldBe(visible).
                shouldHave(exactText("Успешно!\n" + "Встреча успешно запланирована  на " +
                        DataGenerator.generateDate(4)));
    }

    @Test
    void shouldRequireValidNameIfNonCyrillicTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(DataGenerator.
                generateDate(DataGenerator.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue("John Snow");
        $("[data-test-id='phone']").$("[name='phone']").setValue(DataGenerator.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='name'].input_invalid .input__sub").shouldBe(visible)
                .shouldHave(exactText("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @Test
    void shouldRequireAgreementCheckboxTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(DataGenerator.
                generateDate(DataGenerator.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(DataGenerator.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(DataGenerator.generatePhone());
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='agreement'].input_invalid .checkbox__text").shouldBe(visible)
                .shouldHave(exactText("Я соглашаюсь с условиями обработки и использования моих персональных данных"));
    }

    @Test
    void shouldAddPlusToPhoneFieldTest() {
        String validPhone = DataGenerator.generatePhone();
        String withoutPlusPhone = validPhone.substring(1,12);
        validPhone = validPhone.replaceAll("\\s","");
        $("[data-test-id='phone']").$("[name='phone']").setValue(withoutPlusPhone);
        $("[data-test-id='phone']").$("[name='phone']").
                shouldHave(value(DataGenerator.formatPhone(validPhone)));
    }

@Test
void shouldLimitPhoneNumberToElevenNumbersTest() {
    String validPhone = DataGenerator.generatePhone();
    String invalidPhone = validPhone + "0123456789";
    $("[data-test-id='phone']").$("[name='phone']").setValue(invalidPhone);
    $("[data-test-id='phone']").$("[class= 'input__control']").
            shouldHave(value(DataGenerator.formatPhone(validPhone)));
}

    @Test
    void shouldRequireValidPhoneNumberIfTenNumbersTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(DataGenerator.
                generateDate(DataGenerator.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(DataGenerator.generateName("ru"));
        String validPhone = DataGenerator.generatePhone();
        String invalidPhone = validPhone.substring(0,11);
        $("[data-test-id='phone']").$("[name='phone']").setValue(invalidPhone);
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='phone'].input_invalid .input__sub").shouldBe(visible)
                .shouldHave(exactText("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678."));
    }

    @Test
    void shouldRequireValidCityIfEmptyCityFieldTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue("");
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(DataGenerator.
                generateDate(DataGenerator.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(DataGenerator.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(DataGenerator.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='city'].input_invalid .input__sub").shouldBe(visible).
                shouldHave(exactText("Поле обязательно для заполнения"));
    }

    @Test
    void shouldRequireValidDateIfEmptyDateFieldTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateValidCity());
        $("[data-test-id= 'name']").$("[name ='name']").setValue(DataGenerator.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(DataGenerator.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='date'] .input__sub").shouldBe(visible).
                shouldHave(exactText("Неверно введена дата"));
    }

    @Test
    void shouldRequireValidNameIfEmptyNameFieldTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(DataGenerator.
                generateDate(DataGenerator.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue("");
        $("[data-test-id='phone']").$("[name='phone']").setValue(DataGenerator.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='name'].input_invalid .input__sub").shouldBe(visible)
                .shouldHave(exactText("Поле обязательно для заполнения"));
    }

    @Test
    void shouldRequireValidPhoneNumberIfEmptyPhoneFieldTest() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(DataGenerator.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(DataGenerator.
                generateDate(DataGenerator.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(DataGenerator.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue("");
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='phone'].input_invalid .input__sub").shouldBe(visible)
                .shouldHave(exactText("Поле обязательно для заполнения"));
    }
}
