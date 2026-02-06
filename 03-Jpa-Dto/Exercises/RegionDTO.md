### **Part 1: Setup & Data Modeling**

#### **1. Initialize the Project**

We are building a Spring Boot application that will act as the backend for our Region and Municipitality data. We start by creating a Spring project and choosing the 4 dependencies we usually pick.

**Task:** Initialize a new Spring Boot project.

- **Project Name:** RegionDTO
- **Language:** Java Version 25
- **Spring Boot:** Version 4.0.2
- **Build Tool:** Maven
- **Packaging:** Jar
- **Dependencies:**
  - **Spring Web**
  - **Spring Data JPA**
  - **H2 Database**
  - **MySQL Driver**

![Setup 1](assets/images/Setup-1.png)

![Setup 2](assets/images/Setup-2.png)



------



#### **2. The Domain Layer (Entity)**

**Basics:**

- **`@Entity`**: Marks this class as an entity. Hibernate scans for this annotation to create the table.
- **`@Id`**: Every entity *must* have a primary key to uniquely identify rows.

**Task: Create the Region Entity** Create a package named `model` and inside it, create a class named `Region`. We require three specific fields to match our incoming data:

1. `kode` (The region code, e.g., "1084")
2. `navn` (The region name, e.g. "Hovedstaden")
3. `href` (A link to more resources)

**Instructions:**

1. Add the three fields as `String`.
2. Annotate the class to make it a database table.
3. Decide which field is the Primary Key (`@Id`). Since region codes are unique strings, you can use `kode` as the `@Id`, or you can add a separate `Long id` field. **For this exercise, let's treat `kode` as our functional ID.**
4. Generate standard Getters and Setters.
5. Generate a Constructor (**Tip:** When we make our own constructor we also need to add another constructor).

(**Question:** In the last tutorial we didn't include the Id in the constructor or make a Setter for it as the database generated it, does anyone know **why we include it today?**)

Java

```
package com.example.regiondto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

// TODO: Annotate this class so JPA knows it represents a database table
public class Region {

    // TODO: One of these fields must be the Primary Key. Annotate it with @Id.
    private String kode;
    private String navn;
    private String href;

    // TODO: JPA requires an empty constructor. Create one here.

    // TODO: Create a constructor that initializes all fields.

    // TODO: Generate standard Getters and Setters for all fields below.
}
```



------



#### **3. The Data Access Layer (Repository)**

**Basics:** As we have seen JPA provides an interface called `JpaRepository`. By extending this interface, Spring automatically generates implementations for standard CRUD operations (Create, Read, Update, Delete) at runtime. 

**Task: Create the Repository**

1. Create a package named `repository`.
2. Create an **Interface** (not a class) named `RegionRepository`.
3. Make it extend `JpaRepository`.
   - *Hint:* `JpaRepository` takes two generic types: `<EntityClass, PrimaryKeyType>`. Look at your `Region` class—what is the data type of your `@Id`?

(**Bonus info:** The reason we use generics is so we enforce type safety at compile time, e.g. if JpaRepository does not get an EntityClass but instead a String the compiler complains. Also we avoid manual casting).

Java

```
package com.example.regiondto.repository;

import com.example.regiondto.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO: Make so this interface extends JpaRepository taking Region and the ID type (String) as generics
public interface RegionRepository {
    // No code needed here, Spring implements CRUD without us writing it.
}
```



------



#### **4. Database Configuration**

Spring Boot needs to know how to talk to your database. We configure this in `src/main/resources/application.properties`.

**Task: Configure MySQL**

1. Open `application.properties`.
2. Add the configuration for your local MySQL server.
3. **Note:** Start by setting it to whatever your root is (set during install, or use the user you made last time, in the last tutorial I used username: 'jens', password: 'x'. You can also try 'root' and then password blank. If all else fails you can quickly restore the password as we haven't stored important data there yet).
4. **Setting:** `spring.jpa.hibernate.ddl-auto=update`. This tells Hibernate (the JPA provider) to automatically check your `Region` entity and create/update the table in the database when the application starts, why do we use **update** today and not create-drop?

Properties

```
# Database Connection (Set url, username, and password to match your MySql setup)
spring.datasource.url=jdbc:mysql://localhost:3306/region_dto?serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA / Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

**Database Verification Task:**

1. Create the schema `region_dto` in your MySQL Workbench/DataGrip.
2. Run the application.
3. Read the console(last few lines), you can see Hibernate created the table.
4. Open MySQL Workbench/DataGrip, refresh schemas, and **verify that the `region` table exists.**



------



### **Part 2: The Service Layer & Integration**

Now we build the bridge between our application and the external API. We need to fetch raw data from the Region API, convert it into our Java objects, and persist it to our database.

#### **1. Configuration: The RestClient**

Before we can fetch data, we need a tool to make HTTP requests (like a programmatic web browser). Spring Boot 3.2 introduced `RestClient`, a modern, fluent interface for calling REST APIs.

**Basics: Why do we need a Configuration class?** We *could* create a `new RestClient()` inside every service class, but that is bad architecture.

1. **Violation of DRY (Don't Repeat Yourself):** You would have to copy-paste the base URL (`https://api.dataforsyningen.dk`) everywhere.
2. **Coupling:** It makes your code hard to test because you can't easily swap the real internet connection for a fake one during unit tests.

Instead, we use a **Configuration Class**. This tells Spring to create **one** pre-configured `RestClient` instance (a "Bean") and manage it for us. Whenever we need it, Spring hands us this ready-to-use copy.

**Task: Create the Configuration**

1. Create a package named `config`.
2. Create a class named `RestClientConfig`.
3. Copy the code below. Note the usage of `@Configuration` and `@Bean`.

Java

```
package com.example.regiondto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // We configure the Base URL once here. 
        // Any service using this client only needs to specify the specific endpoint (e.g., "/regioner").
        return RestClient.create("https://api.dataforsyningen.dk");
    }
}
```



------



#### **2. The Interface (The Contract)**

We define *what* our service does before we define *how* it does it. We are "programming to an interface".

**Task: Create the Service Interface**

1. Create a package named `service`.
2. Create an interface named `RegionApiService`.
3. Define a single method that returns a list of Regions.

Java

```
package com.example.regiondto.service;

import com.example.regiondto.model.Region;
import java.util.List;

public interface RegionApiService {
    List<Region> getRegions();
}
```



------



#### **3. The Implementation (Service Layer)**

Now we write the integration between the **External API** and our **Database Repository**.

**Basics: Why Constructor Injection?** You often see tutorials using `@Autowired` directly on fields (**Field Injection**). This works fine, but we will use **Constructor Injection** today for three reasons:

1. **Immutability:** By using a constructor, we can mark our fields as `final`. This ensures that once the Service is created, its dependencies can never be changed or broken.
2. **Testability:** If you try to write a Unit Test for a class with Field Injection, you have to use complex "reflection hacks" to inject the dependencies. With a constructor, you pass in the Mock objects (like a fake database) directly.
3. **Contract:** A constructor acts as a contract. It tells the compiler: *"You are not allowed to create a RegionApiService unless you give me a RestClient and a Repository."* This prevents the class from ever existing in an invalid "half-baked" state.

**Basics: Error Handling (`onStatus`)** When we talk to external servers, things can go wrong (e.g., the server is down or the page is missing).

- In the code below, you will see us **chain** multiple methods together.
- We use `.onStatus()` to "listen" for errors. If the API returns a 4xx (Client Error) or 5xx (Server Error), we interrupt the flow and throw an exception. This prevents our app from crashing silently.

**Task: Implement `RegionApiServiceImpl`**

1. Create the class `RegionApiServiceImpl` in the `service` package.
2. Implement the `RegionApiService` interface.
3. **Dependencies:** Add the `private final` fields and generate the constructor.
4. **The Logic:** Copy the code inside `getRegions` carefully. Read through all the lines with `.` —this is the "chaining."

Java

```
package com.example.regiondto.service;

import com.example.regiondto.model.Region;
import com.example.regiondto.repository.RegionRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class RegionApiServiceImpl implements RegionApiService {

    private final RestClient restClient;
    private final RegionRepository regionRepository;

    public RegionApiServiceImpl(RestClient restClient, RegionRepository regionRepository) {
        this.restClient = restClient;
        this.regionRepository = regionRepository;
    }

    @Override
    public List<Region> getRegions() {
        // We use a "Fluent Interface" here. 
        // We chain methods one after another to build the request.
        List<Region> regions = restClient.get()
                .uri("/regioner")
                .header("Accept-Encoding", "identity")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("Client Error: " + response.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Server Error: " + response.getStatusCode());
                })
                .body(new ParameterizedTypeReference<List<Region>>() {});

        // Save to Database (Batch Operation)
        if (regions != null) {
            regionRepository.saveAll(regions);
        }

        return regions;
    }
}
```



------



#### **4. The Controller (The Trigger)**

Now we have a functional service, but no way to run it. We need something to call it, we need an **Endpoint**. When a user visits our endpoint, it triggers our Service to go and fetch the data.

**Task: Create the RegionController**

1. Create a package named `controller`.
2. Create a class `RegionRestController`.
3. Annotate it with `@RestController` (Makes it a web handler) and `@RequestMapping("/regioner")` (This gives all endpoints a common prefix).
4. **Inject the Service:** Add a `private final RegionApiService` field and generate the constructor.
5. **Create the Endpoint:**
   - Create a method `public List<Region> fetchRegions()`.
   - Annotate it with `@GetMapping("/hent")`.
   - Inside, call `regionApiService.getRegions()` and return the result.

**Verification:** Run your application. Open your browser and go to `http://localhost:8080/regions/hent`.

- **Success:** You should see a JSON list of Danish regions.
- **Double Check:** Check MySQL Workbench/DataGrip (refresh the schemas). The `region` table should now be full of data.

![MySQL 1](assets/images/MySQL-1.png)



------



### **Part 3: Relationships & JSON Recursion (Revisited)**

We now have Regioner in our database. But as you know a Region contains Kommuner. We need to model this relationship and fetch that data too.

#### **1. The `Kommune` Entity**

We need to create a new table for kommuner that links back to the region table.

**Task: Create the Entity**

1. Create a class named `Kommune` in the `model` package.
2. Add the `@Entity` annotation to the class.
3. Add the standard fields:
   - `@Id` `@Column(length = 4)` `private String kode;`
   - `private String navn;`
   - `private String href;`
4. **The Relationship:** We need to link this class to `Region`.
   - Create a private field `private Region region;`.
   - Annotate it with `@ManyToOne` (Many Kommuner to One Region).
   - Annotate it with `@JoinColumn(name = "region_kode")` (Names our foreign key column in the database).
5. **Boilerplate:**
   - Generate an empty constructor.
   - Generate a full constructor (selecting all 4 fields).
   - Generate Getters and Setters for all 4 fields.

Java

```
package com.example.regiondto.model;

import jakarta.persistence.*;

@Entity
public class Kommune {
    // TODO: Add fields for kode, navn, href. Remember annotations for @Id and @Column length.

    // TODO: Add the ManyToOne relationship to Region here.

    // TODO: Generate Constructors, Getters, and Setters.
}
```



------



#### **2. Update the `Region` Entity**

##### **Basics: Bidirectional Relationships, Sets & Cascading**

- **Why Bidirectional?** In a database, only the child (`kommune`) knows who its parent is. The parent (`region`) knows nothing. By adding a list to the Region class, we make the relationship **Bidirectional**. This allows us to easily ask: *"Give me Region X and all its municipalities"* in a single Java command.
- **Why `Set` instead of `List`?** A `List` allows duplicates. It is impossible for the same Municipality to belong to a Region twice, a `Set` automatically enforces this. We also avoid issues if adding more relationships later.
- **Why `CascadeType.ALL`?** This links the **lifecycle** of the child to the parent.
  - If we **save** a Region, it automatically saves any new Kommuner added to it.
  - If we **delete** a Region, it automatically deletes all its Kommuner. This prevents "Orphaned Data" (municipalities that point to a region that no longer exists).

**Task: Add the Set**

1. Open your existing `Region.java` class.
2. **Add the Field:** Create a `private Set<Kommune> kommuner = new HashSet<>();`.
   - *Note:* Always initialize it with `new HashSet<>()` to avoid null errors.
3. **Add the Annotation:**
   - Add `@OneToMany` above the field.
   - Inside the annotation parentheses, set `mappedBy = "region"`. (This tells Hibernate: "Go look at the 'region' field in the Kommune class to understand how to handle this connection").
   - Also add `cascade = CascadeType.ALL`.
4. **Boilerplate:** Generate the Getter and Setter for this new `kommuner` field.

Java

```
// Inside Region.java

import java.util.HashSet;
import java.util.Set;

// ... existing fields ...

    // TODO: Add the Set<Kommune> field with @OneToMany annotation here.

    // TODO: Generate Getter and Setter for 'kommuner'.
```



------



#### **3. Fetching the Kommuner**

Now we need to fetch the municipality data. We quickly set up the repository and copy the fetch logic.

**Task 3a: Create the Repository**

1. Create a new interface `KommuneRepository` in the `repository` package.
2. Make it so it extends `JpaRepository<Kommune, String>`.

**Task 3b: Update the Service Layer:** We need to use this new repository in our Service.

1. Open `RegionApiServiceImpl`.

2. **Add Field:** Add `private final KommuneRepository kommuneRepository;` to the class fields.

3. **Update Constructor:** You must now inject this new repository, add it to the Constructor.

4. **Update the Interface:**

   - Open `RegionApiService.java` (The Interface file).
   - Add this line: `List<Kommune> getKommuner();`

5. **Implement the Logic:**

   - Go back to `RegionApiServiceImpl.java`.

   - Copy the code block below into the class.

Java

```
    @Override
    public List<Kommune> getKommuner() {
        List<Kommune> kommuner = restClient.get()
                .uri("/kommuner")
                .header("Accept-Encoding", "identity")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Kommune>>() {});

        if (kommuner != null) {
            kommuneRepository.saveAll(kommuner);
        }
        return kommuner;
    }
```



------



#### **4. Update the Controller**

Finally, add a trigger so we can run this code.

**Task:**

1. Open `RegionRestController`.

2. Add a new endpoint method.

   - Annotation: `@GetMapping("/kommuner")`.
   - Method Signature: `public List<Kommune> fetchKommuner()`.
   - Body: Return `regionApiService.getKommuner();`.

   

------



### **Part 4: We check if it works**

We now need to check if everything works and we can store some Kommuner - I really hope so :). Kommuner is not the most exciting thing to store, but using todays approach we can fetch, manipulate and later on show any data we want.

**Step 1: Run the Fetch**

1. Start application.
2. First, ensure Regions are loaded by visiting: `http://localhost:8080/regions/hent`
3. Next, load the Municipalities by visiting: `http://localhost:8080/regions/kommuner` (should match your controller mapping).

**Step 2: Database Check** Open MySQL Workbench / DataGrip.

1. Check the `region` table: It should have 5 rows.
2. Check the `kommune` table: It should have ~98 rows.
3. **The Key Check:** Look at the `region_kode` column in the `kommune` table. It should be filled with data (e.g., '1084'), proving the relationship is working.

![MySQL 2](assets/images/MySQL-2.png)

**Step 3: The "Trap" (I know we already tried this, this is the last time i swear)**. We have successfully ingested the data. But lets say we wanted to display a list of Regions to a user on a frontend.

If we where to create a simple endpoint like this:

Java

```
@GetMapping("/regions")
public List<Region> getAllRegions() {
    return regionRepository.findAll();
}
```

**Hint:** If you want to test it, the regionRepository needs to be added to the class (field and constructor).

**What would happen?**

1. Java tries to convert a `Region` to JSON.
2. It sees the `Region` has a list of `Kommuner`, so it tries to convert those.
3. It goes inside a `Kommune` and sees it belongs to a `Region`.
4. It goes back to the `Region`...
5. **CRASH:** `StackOverflowError` (Infinite Recursion).

**Note:** This is our last Recursion lesson I promise :). To fix our Infinite Recursion issue, we can use **@JsonBackReference** (tells Jackson to ignore the relationship field), that fixes the problem. But now we have a **Kommune JSON missing the Region field**, we also want to **decouple our Database Structure from our Public API** - so that if we change something(e.g. a column name) it does not break our frontend.



#### **Questions:**

1. We never wrote any code to link a Kommune to a Region (e.g. `kommune.setRegion(...)`). How did the database know how to assign say 'Roskilde' to 'Region Sjælland'?
2. We know that Regions and Municipalities are linked. So why did our two endpoints (`/regioner` and `/kommuner`) work fine just now without triggering an infinite loop?



**Next Up:** We will solve this by again using **DTOs (Data Transfer Objects)** - safe, flat copies of our data designed specifically for the API.

------

