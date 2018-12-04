# Object and data properties in domain class plugin view.
The plugin was developed by ODASE Team, for more information about us, see http://www.odaseontologies.com.
The views are available at:
1. Windows -> Views -> Object Properties Views -> Object properties in Domain of class view
2. Windows -> Views -> Data Properties Views -> Data properties in Domain of class view

The views shows the data/object properties for which the domain is super-class to the selected OWL class, in other words
all properties, such that selected class ⊆ domain(property)
The views also allows to add new properties/subproperties or delete existing ones. When adding a new property, its domain will be automatically set to the selected OWL class.
It is advised to run a reasoner in order to obtain a more complete tree of the data/object properties.
Both views are synchronised with the rest of the view. So if you click on one the of the properties, the rest of the related views will be updated.

#### Prerequisites

To build and run, you must have the following items installed:

+ Java 8 or higher
+ Apache's [Maven](http://maven.apache.org/index.html).
+ A Protégé distribution 5.0 - 5.2.0. The Protege releases are [available](http://protege.stanford.edu/products.php#desktop-protege) from the main Protege website.

#### Build and install plug-ins

1. Type mvn clean package.  On build completion, the "target" directory will contain a protege.plugin.examples-${version}.jar file.

2. Copy the JAR file from the target directory to the "plugins" subdirectory of your Protégé distribution.
 
#### Plug-in screenshots
The screenshot shows how this views can be used in combination with the existing views.
![screenshot1](https://user-images.githubusercontent.com/19971537/27334926-24f65a3a-55cb-11e7-87fa-5b1fa1ea54a4.JPG)
