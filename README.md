# Lightwave

## INTRODUCTION
"What if we were able to build Habbo Retros which are actually good?" Certainly, a successful private server requires a loyal community, an intelligent leadership and a stubborn will. Nonetheless we may not forget its dependence on technology that opens a door to infinity, if implemented properly. Currently, realizing concepts and ideas which require technical operations is way too complicated. Additionally, synchronization issues and further bugs bother both the community and the management. Lightwave, a private Habbo server, aims for change.

### **How do we want to achieve change?**
By using the Actor model for synchronization purposes and scaling, unit tests that define functionality, version control and a modular design, we can get a maintainable system that scales out and up and makes it easy to extend functions due to its modular design (e.g. implementing a multiversion hotel).

### **A multiversion hotel?**
The advantage of splitting up server components into modules lies in their reusability. This way, front-end servers which handle the actual clients and their messages can be separated from the game logic. By doing this, we are enabled to create multiple front-end servers supporting different versions (e.g. Shockwave v18, v26 up to Flash r63).

## TECHNOLOGY
The majority of the project should be implemented using Scala and the popular Actor library "Akka" which includes clustering. Nevertheless, scripts and sub-projects can be programmed in any language.

