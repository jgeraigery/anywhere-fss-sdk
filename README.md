# HCP Anywhere File Sync and Share SDK                                                                  
                                                                                                    
The HCP Anywhere File Sync and Share API is an API for connecting to HCP Anywhere. This SDK provides convenient access to the HCP Anywhere FSS API. 
                                                                                                    
In order to make calls to the sub APIs, you must acquire an AuthToken. An AuthToken can be acquired from the server by calling the authenticate method, which requires you to provide the username and password you use to access HCP Anywhere. The returned AuthToken can be saved for later use.
                                                                                                    
Once an AuthToken is acquired, you can make calls to the sub APIs by supplying the token and any other required arguments of the sub APIs.


# Documentation 

Javadoc is available as a JAR via Maven Central or [via github](http://hitachi-data-systems.github.io/anywhere-fss-sdk/javadoc/).

## Example                                                                                          
                                            
```
AnywhereAPI api = new AnywhereAPI.Builder("https://myanywhere.example.com/").build();
AuthToken authToken = api.authenticate("jdoe", "passwd");
User me = api.getUserAPI().getInfo(authToken);
```

A complete example is provided in the repository under /example.

## Using the HCP Anywhere File Sync and Share SDK                                                       

The SDK is availble at Maven Central.


# Questions?

Reach out to Hitachi Data Systems at our community portal http://community.hds.com.

# Copyright and License

Code and documentation copyright by Hitachi Data Systems, 2016.  Release under [the Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).