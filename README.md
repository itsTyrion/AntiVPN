# AntiVPNVelocity
Blocks joining with VPNs, proxies, and from some hosters

Config explanation:
------

* **kickMessage**: The message players will see when disconnected for using a VPN/Proxy.
* **ipCacheDuration**: For how many hours will the plug-in cache whether an IP is considered *bad*.
* **logFailedAttempts**: If a connection is blocked, log the Player's name + IP?
* **preLogin**: Check the IP before the login request is completed. This is very lightweight but **DOES NOT** allow bypassing via permission.
* **bypassPermission**: If a player has this permission, they can join using a VPN or similar. Use with care. *preLogin* has to be **false**.
* **ipHub-Key**: The plug-in uses iphub.info, which requires an API key. Get a free plan [here](https://iphub.
  info/apiKey/newFree) (or paid, in case you have more than 1k *unique* players per day)
* **allowConnectionWhenExceeded**: Once your limit has been reached, IPHub won't answer further requests for the day.
  The plug-in defaults to letting every player join, but you can change it to block (not recommended)


FAQ:
------

### Does VPN service x bypass?
It shouldn't, but I can't assure you. There are new anonymization services with more IPs each by the day.

### Why is preLogin more lightweight/Why can't it be used with the bypass permission?
When a player tries to log in, their username is being sent, but not their UUID, which has to be requested from Mojang.
This also implies that someone with ill intentions can't "give" your server a temporary request block ("rate limit", ~10 min) by spamming logins and aborting them.

Since no UUID is known before fully processing the login, the permission system used doesn't have any data for the player loaded, yet.

### Why is there a limit to 1000 players per day?
Limitation of the IPHub free plan. If you have joins from more than 1000 IPs/day, you need to get a paid plan. 
Nothing I can do about it.

### Why do I even need that? It used to just work
Sadly, the old database (iplegit.com) just went offline one day, no idea what happened :(

