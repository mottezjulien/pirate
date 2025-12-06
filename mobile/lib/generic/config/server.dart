
class Server {

  static const String httpAPI = 'http://192.168.1.27:8080';
  static const String wsAPI = 'ws://192.168.1.27:8080';

  //static const String httpAPI = 'https://app-63bb8b44-e760-4d5e-8337-b9f7689d76c2.cleverapps.io';
  //static const String wsAPI = 'ws://app-63bb8b44-e760-4d5e-8337-b9f7689d76c2.cleverapps.io';

  static Uri uri(String resourcePath) {
    return Uri.parse('$httpAPI$resourcePath');
  }

}