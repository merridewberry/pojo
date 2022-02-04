import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class MainTest {
    static final String POST_URI = "https://restful-booker.herokuapp.com/booking";
    static final String GET_URI = "https://restful-booker.herokuapp.com/booking/";

    static BookingDates bookingDates = new BookingDates("2021-08-31", "2021-09-10");
    static Booking booking = new Booking("John", "Doe", 100, true, bookingDates, "Breakfast");

    @Test
    public void serializeTest() {
        post();
    }

    @Test
    public static void deserializeTest() {
        BookingInfo bookingInfo = post().extract().body().as(BookingInfo.class);

        Booking checkBooking = given()
                .get(GET_URI + bookingInfo.getBookingid())
                .then()
                .extract().body().as(Booking.class);

        Assert.assertEquals(checkBooking.toString(), bookingInfo.getBooking().toString());
    }

    private static ValidatableResponse post() {
        return given()
                .contentType("application/json")
                .accept("application/json")
                .body(booking)
                .post(POST_URI)
                .then()
                .statusCode(200);
    }


}
