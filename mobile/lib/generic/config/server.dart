

enum ServerType {
  auth, config, session;
}

class Server {


  static const String httpAPI = 'http://192.168.1.27:8080';
  static const String wsAPI = 'ws://192.168.1.27:8080';

  //static const String httpAPI = 'https://app-63bb8b44-e760-4d5e-8337-b9f7689d76c2.cleverapps.io';
  //static const String wsAPI = 'ws://app-63bb8b44-e760-4d5e-8337-b9f7689d76c2.cleverapps.io';

  //static const String httpAPI = 'https://api.lockedout.fr';
  //static const String wsAPI = 'ws://api.lockedout.fr';

  static Uri uri(ServerType serverType, String resourcePath) {
    switch(serverType) {
      case ServerType.auth:
        return auth(resourcePath);
      case ServerType.config:
        return config(resourcePath);
      case ServerType.session:
        return session(resourcePath);
    }
  }

  static Uri auth(String resourcePath) {
    return Uri.parse('$httpAPI$resourcePath');
  }

  static Uri config(String resourcePath) {
    return Uri.parse('$httpAPI$resourcePath');
  }

  static Uri session(String resourcePath) {
    return Uri.parse('$httpAPI$resourcePath');
  }

}