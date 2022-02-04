## What is POJO

POJO stands for Plain Old Java Object, it's a simple Java object not restricted by any framework limitations (except for basic Java specifications of course) and viable in any environment.

POJOs offer for direct and flexible approach to data serialization and deserialization, so their main role in testing with RestAssured is to process JSONs both in the request and response bodies.

They allow you to set up a request JSON containing just about any data you may need. For example, you can use a single POJO class for multiple similar JSONs with slightly different set of fields, so you can avoid repeating the same code all over again. Or you can create multi-level nested JSONs keeping their structure simple and easy to understand.

When working with response bodies, POJOs also allow you to extract any value you may need and access nested data -- and all that, as already stated above, in an easy-to-understand way and without repeating the same code multiple times. 


## How to create a POJO and what to do with it afterwards

POJO is a public class with private variables, and to access or modify them you have to use setter and getter methods. POJO also needs at least a default constructor to specify the set of arguments for any instance of this POJO. So in the most basic way a POJO will look like this:

    public class Book {

        private String title;
        private String author;

        public Book() {
	    }

	    public String getTitle() {
		    return title;
	    }

        public void setTitle(String title) {
		    this.title = title;
	    }

        public String getAuthor() {
		    return author;
	    }

        public void setAuthor(String author) {
		    this.author = author;
	    }
    }

As with any other Java method, you can overload constructors. This way you can make a few sets of arguments you may need in your POJO instances. For example these constructors allow you to create just a book without an author and a title or a book with both an author and a title, but not anything in-between:

	public Book() {
	}

	public Book(String title, String author) {
		this.title = title;
		this.author = author;
	}

Though nothing stops you from creating a book with default empty constructor and then adding just one argument to it using setter method.

You can create an instance of your POJO in a following manner:

    Book book1 = new Book();

or:

    Book book2 = new Book("War and Peace", "Leo Tolstoy");

## Putting POJO into practice

To demonstrate how POJOs work I will use Restful Booker API, you can find the documentation here: https://restful-booker.herokuapp.com/apidoc/index.html

Both request and response bodies in this API utilise JSON with the similar structure:

    {
        "firstname" : "Jim",
        "lastname" : "Brown",
        "totalprice" : 111,
        "depositpaid" : true,
        "bookingdates" : {
            "checkin" : "2018-01-01",
            "checkout" : "2019-01-01"
        },
        "additionalneeds" : "Breakfast"
    }

To create POJO for this JSON we have to create new Booking class where we will initialize all the variables we need the same way we did it in the previous example.

You may notice that the *bookingdates* field contains two nested values, and we don't have the correct data type right away. This problem is quite easy to solve though -- all you need to do is to create the BookingDates class: 

    public class BookingDates {

        private String checkin;
        private String checkout;

        public BookingDates(String checkin, String checkout) {
            this.checkin = checkin;
            this.checkout = checkout;
        }
    }

Now there is the data type we can use for the *bookingdates* variable and with that we have everything we need to create our Booking class:

    public class Booking {

        private String firstname;
        private String lastname;
        private Integer totalprice;
        private Boolean depositpaid;
        private BookingDates bookingdates;
        private String additionalneeds;

        public Booking(String firstname, String lastname, Integer totalprice, Boolean depositpaid, BookingDates bookingdates, String additionalneeds) {
            this.firstname = firstname;
            this.lastname = lastname;
            this.totalprice = totalprice;
            this.depositpaid = depositpaid;
            this.bookingdates = bookingdates;
            this.additionalneeds = additionalneeds;
        }
    }

You can use the same principle to create POJOs for JSON with multiple levels of nesting.

Since all the variables in both classes are private, and we will need to access them, we should write each of them a getter method:

    public String getFirstname() {
    return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    (and so on)

This concludes all the preparation our POJO will need, so we can proceed to serialization and deserialization.

### Serialization

As an example of serialization with POJO we'll make a JSON for creating a new booking.

To do that we'll have to create instances of the Booking and BookingDates classes with all the required data:

    BookingDates bookingDates = new BookingDates("2021-08-31", "2021-09-10");  
    Booking booking = new Guest("John", "Doe", 100, true, bookingDates, "Breakfast");

RestAssured can convert POJO to JSON as is, so we won't need any additional steps. All we have to do is put our *booking* object in our request body:

    given()
        .contentType("application/json")
        .accept("application/json")
        .body(booking)
        .post("https://restful-booker.herokuapp.com/booking")
        .then()
        .statusCode(200);

This way we are sending the POST request with the following body:

    {
        "firstname" : "John",
        "lastname" : "Doe",
        "totalprice" : 100,
        "depositpaid" : true,
        "bookingdates" : {
            "checkin" : "2021-08-31",
            "checkout" : "2021-09-10"
        },
        "additionalneeds" : "Breakfast"
    }

### Deserialization

To showcase how the deserialization with POJO works we'll create the same booking as before, deserialize the response and extract the value of *bookingid* and then request the details of the booking with this id to compare them with the JSON we've initially sent.

At first let's take a look at the response we're getting after we create a new booking:

    {
        "bookingid": 36,
        "booking": {
            "firstname": "John",
            "lastname": "Doe",
            "totalprice": 100,
            "depositpaid": true,
            "bookingdates": {
                "checkin": "2021-08-31",
                "checkout": "2021-09-10"
            },
            "additionalneeds": "Breakfast"
        }
    }

As you can see this JSON structure became a bit more complicated since it gained one more level. Just as we did before with Booking and BookingDates classes, let's create the new BookingInfo class:

    public class BookingInfo {
    
        private int bookingid;
        private Booking booking;
    
        public BookingInfo() {
        }

        public int getBookingid() {
            return bookingid;
        }
    
        public Booking getBooking() {
            return booking;
        }
    }

Also, we'll need to add empty constructors to the classes we've already created:

    public BookingDates() {
    }

and:

    public Booking() {
    }

Now we can save the response to our POST request as a variable and deserialize it at the same time:

    BookingInfo bookingInfo = given()
        .contentType("application/json")
        .accept("application/json")
        .body(body)
        .post(POST_URI)
        .then()
        .extract().body().as(BookingInfo.class);

As you can see, deserialization is quite similar to serialization -- RestAssured can also convert JSON to POJO without any additional steps.

Now we have the instance of the BookingInfo class which contains all the data we received. In order to check if all the data has been saved properly let's use the *bookingid* we received to get our booking's details:

    Booking checkBooking = given()
        .get("https://restful-booker.herokuapp.com/booking/" + bookingInfo.getBookingid())
        .then()
        .extract().body().as(Booking.class);

The response data was correct but since we don't want to rely just on the visual check, let's compare it to the data we've sent in our POST request. 

The easiest way to do so is to convert our POJO to a string. In order to do that let's override the toString() method in the Booking and BookingDates classes:

    @Override
    public String toString() {
        return "firstname: " + this.firstname + "; lastname: " + this.lastname + "; totalprice: " + this.totalprice + "; depositpaid: " + this.depositpaid + "; bookingdates: " + this.bookingdates + "; additionalneeds: " + this.additionalneeds;
    }

and:

    @Override
    public String toString() {
        return "checkin: " + this.checkin + "; checkout: " + this.checkout;
    }

After that all we have to do is to compare the strings we've got:

    Assert.assertEquals(checkBooking.toString(), bookingInfo.getBooking().toString());

## Useful tools and libraries

### @Getter and @Setter annotations from the Lombok library

Both getter and setter methods take quite a lot of space and barely bring any useful information with themselves -- especially with larger POJOs where they can take up dozens of lines. Lombok library allows you to get rid of all getters and setters completely and replace them with two simple annotations:

Lombok:

    @Getter @Setter
    public class BookingDates {
        private String checkin;
        private String checkout;
    }

Vanilla Java:

    public class BookingDates {
        private String checkin;
        private String checkout;
    
        public String getCheckin() {
            return checkin;
        }
    
        public String getCheckout() {
            return checkout;
        }
    
        public void setCheckin(String checkin) {
            this.checkin = checkin;
        }
    
        public void setCheckout(String checkout) {
            this.checkout = checkout;
        }
    }

### Jackson

###### @JsonInclude

JsonInclude annotation allows you to manage arguments which will be included or excluded from final JSON. You can use this annotation both with the whole class and with specific variables. Here are some values you can use with it: *ALWAYS* will include variable no mater what, *NON_NULL* will exclude variables that are equal to null and *NON_EMPTY* will exclude null values as well as empty lists and arrays.

For example, annotation `@JsonInclude(JsonInclude.Include.NON_NULL)` used for the whole POJO class will allow you to use the same class for multiple different JSON schemas. You'll just have to set the variables you won't need to null and they'll be excluded from the final JSON. 


###### @JsonProperty

JsonProperty annotation allows you to state which getter or setter should be associated with a variable if you have to use a method with a different name for some reason:

    private String checkin;

    @JsonProperty("checkin")
    public String getDate() {
        return checkin;
    }

###### @JsonAlias

JsonAlias allows you to save arguments with different names into the same variable: 

    @JsonAlias({ "firstName", "name" })
    private String firstname;

In this case the *firstname* variable will be used for all three field titles: firstname, firstName и name. This annotation helps quite a lot with inconsistent JSON schemas where same values are used with different names.

###### @JsonIgnoreProperties и @JsonIgnore

Both of these annotations allow you to state which variables should be ignored while serializing or deserializing JSON.

@JsonIgnoreProperties is used for the whole class:

    @JsonIgnoreProperties({ "bookingid" })
    public class BookingInfo {

        private int bookingid;
        private Booking booking;
    }

@JsonIgnore is used for the variable that should be excluded:

        private int bookingid;
        @JsonIgnore
        private Booking booking;

### Package org.hamcrest.beans from the Hamcrest library

###### hasProperty

hasProperty() method allows you to check if an instance of a class contains said property:

    assertThat(bookingInfo, hasProperty("bookingid"))

or:

    assertThat(bookingInfo, hasProperty("bookingid", equalTo(15))

###### samePropertyValuesAs

samePropertyValuesAs() allows you to compare two instances of the same or different classes:

    assertThat(bookingInfo, samePropertyValuesAs(bookingInfoCheck))

Though it's important to mention that this method works correctly only with simple non-nested POJOs.