# REST_StartCode
  GitHub repository containing a REST API template equipped with JWT token generation functionality. Features include resources for basic user management and endpoint testing using REST-assured.

  > ### NOTES:
  > - *Set db name in pom*
  > - *Remember to add annotated classes in hibernateConfig*
  > - *Remember to run docker*
  > - *In tests classes, remember to delete content from tables and reset sequence from 0, then add data*
  > - *Remember to use the Abstract DAO methods*
  > - *Remember that DAO classes return and take as argument entities and Controller classes return DTO's to frontend*
  > - *Remember to initialize the EntityManagerFactory emf in ADAO from any Singleton classes extending it like this:*
  > ```
>      public static ExampleDAO getInstance(EntityManagerFactory _emf) {
>        if (instance == null) {
>            instance = new ExampleDAO();
>        }
>        emf = _emf;
>        ADAO.emf = emf;
>        return instance;
>    }
> ```
> - *Remember to add this before endpoints to verify logged in user:*
>```
> before(securityController.authenticate());
>```



