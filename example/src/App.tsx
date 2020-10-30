import * as React from 'react';
import { StyleSheet, View, Text, Dimensions, ScrollView } from 'react-native';
import TboxCommons from 'tbox-commons';

const windowWidth = Dimensions.get('window').width;
/* const windowHeight = Dimensions.get('window').height; */

const randomColor = () => {
  var letters = '0123456789ABCDEF';
  var color = '#';
  for (var i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)];
  }
  return color;
}


const texts = [
  {text: "aaaaaa", width: windowWidth, fontSize: 16, lineHeight: 20},
  {text: "aaaaaaaaaaaa", width: windowWidth, fontSize: 16, lineHeight: 20},
  {text: "aaaaaaaaaaaaasdadasdasdasdasdasdasd", width: windowWidth, fontSize: 16, lineHeight: 20},
  {text: "aaaaaaaaaaaamaDSBasdfgkgasKDGaksjdkasGDKgasdkgsDKJgksdgkjSGDKJHagsdkgSDHJ\n\n\nsdfsdfsfsdfasdasdasadasdasdasdasdasd", width: windowWidth, fontSize: 16},
  {text: "aaaaaaaaaaaamaDSBasdfgkgasKDGaksjdkas asdasd asdad asdasdaasd adasdasd asdasdasd", height: 20, fontSize: 16},
  {text: "aaaaaaaaaaaamaDSBasdfgkgasKDGaksjdkas asdasd asdad asdasdaasd adasdasd asdasdasd", height: 40, fontSize: 16},
  {text: "aaaaaaaaaaaamaDSBasdfgkgasKDGaksjdkas asdasd asdad asdasdaasd adasdasd asdasdasd", height: 60, fontSize: 16},
]
export default function App() {
  const [result, setResult] = React.useState<number[]>();

  React.useEffect(() => {
    TboxCommons.measure(texts).then(setResult)
  }, []);

  return (
    <View style={styles.container}>
      <ScrollView style={{marginTop: 50, flex:1}}>
      {result && result.map((size: any, index:  number) => { 
        /* console.log(index); */
        return <Text style={{width: texts[index].width ? texts[index].width : size, height: texts[index].width ? size : texts[index].height, fontSize:texts[index].fontSize, backgroundColor: randomColor()}}>{texts[index].text}</Text>
      })}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
