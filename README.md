[![Build Status](https://travis-ci.org/trustedanalytics/space-shuttle-demo.svg)](https://travis-ci.org/trustedanalytics/space-shuttle-demo)
[![Dependency Status](https://www.versioneye.com/user/projects/5723704eba37ce00464e061c/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5723704eba37ce00464e061c)

# space-shuttle-demo
Sample application for ATK space shuttle demo. The default version of that application uses gateway and kafka as a streaming source. If you want to use a mqtt instead look [here](mqtt/README.md)

## Overview
![](wikiimages/space_shuttle_demo.png)

#### Scoring flow:
1. Application space-shuttle-demo listens to kafka topic and waits for feature vectors.
2. When a kafka message appears, application asks Scoring Engine to classify received feature vector.
3. Application stores scoring result in InfluxDB.

#### Generating graph flow:
1. Web application asks backend application (space-shuttle-demo) for a anomalies chart.
2. Space-shuttle-demo gets anomalies (classes different than 1) count per minute from InfluxDB.

## Deploying application to TAP
   
### Manual deployment
1. Upload the model to HDFS: 
   * Download already prepared model from [https://s3.amazonaws.com/trustedanalytics/v0.7.1/models/space-shuttle-model.tar](https://s3.amazonaws.com/trustedanalytics/v0.7.1/models/space-shuttle-model.tar)
   * Login to TAP console and select `Data catalog` page in the navigation panel
   * Select `Submit Transfer` tab
   * Select input type: `Local path` and choose previously downloaded model
   * Enter `Title`
   * Click `Upload` 
   
   Alternatively, you can create TAP Analytics Toolkit model by yourself. Please refer to the [instructions](#creating-tap-analytics-toolkit-model).
   
1. Create required service instances (if they do not exist already). 
   Application will connect to these service instances using Spring Cloud Connectors. Note: If you use the recommended names of the required service instances they will be bound automatically with the application when it is pushed to Cloud Foundry. Otherwise, service instances names will need to be adjusted in `manifest.yml` file or removed from `manifest.yml` and bound manually after application is pushed to Cloud Foundry.
    1. Instance of InfluxDB (recommended name: `space-shuttle-db`)
    1. Instance of Zookeeper (recommended name: `space-shuttle-zookeeper`)
    1. Instance of Gateway called (recommended name: `space-shuttle-gateway`)
    1. Instance of Scoring Engine with recommended name: `space-shuttle-scoring-engine`. Instructions below describe how to create the Scoring Engine service instance.

   To create Scoring Engine service instance take the following actions:
     * Select `Marketplace` tab in TAP Console navigation panel
     * Select `TAP Scoring Engine` service offering
     * Type name `space-shuttle-scoring-engine`
     * Click `+ Add an extra parameter` and add TAP Analytics Toolkit model url:
        key: `uri`
        value: `hdfs://path_to_model`
     * Click `Create new instance`

1. Create Java package:
  ```
  mvn package
  ```
1. (Optional) Edit the auto-generated `manifest.yml`. If you created service instances with different names than recommended, adjust names of service instances in `services` section to match those that you've created. You can also remove `services` section and bind them manually later. You may also want to change the application host/name.
1. Push application to the platform using command:
  ```
  cf push
  ```
1. (Optional) If you removed `services` section from `manifest.yml` application will not be started yet. Bind required service instances (`cf bind-service`) to the application and restage (`cf restage`) the application.
1. The application is up and running

### Automated deployment
* Switch to `deploy` directory: `cd deploy`
* Download [the model](https://s3.amazonaws.com/trustedanalytics/v0.7.1/models/space-shuttle-model.tar) and rename it to `model.tar` 
* Install tox: `sudo -E pip install --upgrade tox`
* Run: `tox`
* Activate virtualenv with installed dependencies: `. .tox/py27/bin/activate`
* Run deployment script: `python deploy.py`, the script will use parameters provided on input. Alternatively, provide parameters when running script. (`python deploy.py -h` to check script parameters with their descriptions).

## Sending data to Kafka

To send data to kafka through a gateway you can either push space_shuttle_client from client directory to space with existing gateway instance or use python `space_shuttle_client.py` locally passing gateway url as a parameter.

### Running on Cloud Foundry:

1. Login to space containing `space-shuttle-gateway`.
2. Go to: `client/`
3. Push app to Cloud Foundry using `cf push`.
<br /> _**Note**: in case of name conflict during push add name parameter `cf push <custom_name>`_

### Local configuration:

#### Prerequisites:

1. Python 2.7
2. tox ([installation details](http://tox.readthedocs.io/en/latest/install.html))

#### Gateway URL

To determine URL of the gateway you are going to send data to:

1. Go to `Applications` page
2. Search for `space-shuttle-gateway`
3. Copy the application URL
   
#### Running python client locally:

1. Go to: `client/` 
2. Run tox: `tox`
3. Activate created virtualenv: `. .tox/py27/bin/activate` 
4. Run: `python space_shuttle_client.py --gateway-url <gateway_url>`

##Creating TAP Analytics Toolkit model
To create the model for Scoring Engine take the following actions: 

#### Upload training data set to HDFS
   * Login to TAP console and select `Data catalog` page in the navigation panel
   * Select `Submit Transfer` tab
   * Select input type: `Local path`
   * Select sample training data file which can be found here: [src/main/atkmodelgenerator/train-data.csv](src/main/atkmodelgenerator/train-data.csv))
   * Enter `Title`
   * Click `Upload` 

When upload is completed, click `Data sets` tab and view the details of uploaded data set by clicking its name.
Copy the value of `targetUri` which contains path to the uploaded data set in HDFS - you will need this to create TAP Analytics Toolkit model in Jupyter notebook.

#### Create TAP Analytics Toolkit instance
   * In TAP console select `Data Science` and then `TAP Analytics Toolkit` tab
   * If there is an instance of `TAP Analytics Toolkit` installed you will see it in an instances list - no action needed. If there are no instances you will be asked if you want to create one - select `Yes`, wait until the application is created (it can take about a minute or two). The application will appear in `TAP Analytics Toolkit` instances list

#### Create Jupyter instance
   * In `Data Science` tab select `Jupyter`. Create new `Jupyter` instance.
   * Copy the password for created Jupyter instance.
   * Login to Jupyter by clicking `App Url` link. 

#### Install TAP Analytics Toolkit client
   * Create new notebook
   * Install TAP Analytics Toolkit client in your notebook by executing command: `!pip install <link-to-atk-server>/client`. `<link-to-atk-server>` can be copied from URL column in `TAP Analytics Toolkit` instances list.

#### Connect to TAP Analytics Toolkit server and run model generation script
   * Copy the contents from [src/main/atkmodelgenerator/atk_model_generator.py](src/main/atkmodelgenerator/atk_model_generator.py) into your notebook. 
   * After imports section set the URI to TAP Analytics Toolkit server: `ta.server.uri = <link-to-atk-server>`
   * Set the value of `ds` as the link to the data set previously uploaded to HDFS (`targetUri`).
   * Run the script. The link to the created model in HDFS will be printed in the output.

## Local development
   * The application can be run in three different configurations depending on chosen data provider (streaming source).
   * There is one special Spring `@Profile` (local) which was created to enable local development
   * cloud, kafka and mqtt profiles should be inactive while local development
   * random profile should be active instead while local development. It uses a simple random number generator instead of streaming source like kafka or mqtt
   * **Note**: Streaming data here are random numbers so it generates a lof of anomalies

### Local Configuration
#### Prerequisites
##### InfluxDB
   * You can find instruction how to install and run InfluxDB here: http://influxdb.com/docs/v0.8/introduction/installation.html
   * The easiest way is to run it in into docker container `docker run -d -p 8083:8083 -p 8086:8086 tutum/influxdb:0.8.8`
   * **Note**: `influxdb:0.9` is **NOT** backwards compatible with `0.8.x.`
   
##### Scoring Engine  
Instruction on how to push scoring engine on the platform: [instruction](#manual-deployment)
  
#### Run
   * 1. Make sure that both local and random profiles are active
   * 2. `export SE_URL <scoring engine URL>` **NOTE:** link should not contain `http://` protocol
   * 3. `mvn spring-boot:run`
   * 4. In a web browser enter `localhost:8080`

