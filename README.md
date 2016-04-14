# Anywhere File Sync and Share SDK                                                                  
                                                                                                    
HCP Anywhere FSS API is an API for connecting to HCP Anywhere, accessible via HTTP. This SDK provides convenient access to the HCP Anywhere FSS API. 
                                                                                                    
In order to make calls to the sub APIs, you must first authenticate with HCP Anywhere to retrieve an AuthToken. An AuthToken can be acquired from the server by calling the authenticate method, which requires you to provide the username and password you use to access HCP Anywhere. The returned AuthToken can be saved for later use.
                                                                                                    
Once an AuthToken is acquired, you can make calls to the sub APIs by supplying the token and any other required arguments of the sub APIs.
                                                                                                    
## Example                                                                                          
                                            
```
AnywhereAPI api = new AnywhereAPI.Builder("https://myanywhere.example.com/").build();
AuthToken authToken = api.authenticate("jdoe", "passwd");
User me = api.getUserAPI().getInfo(authToken);
```

## Using the Anywhere File Sync and Share SDK                                                       

The SDK is availble at Maven Central.

# Copyright and License

Code and documentation copyright by Hitachi Data Systems, 2016.  Release under [the Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).