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
  {text: "aaaaaa", width: windowWidth, fontSize: 16, lineHeight: 25},
  {text: "aaaaaaaaaaaa", width: windowWidth, fontSize: 16, lineHeight: 25},
  {text: "aaaaaaaaaaaaasdadasdasdasdasdasdasd", width: windowWidth, fontSize: 16, lineHeight: 25},
  {text: "aaaaaaaaaaaamaDSBasdfgkgasKDGaksjdkasGDKgasdkgsDKJgksdgkjSGDKJHagsdkgSDHJ\n\n\nsdfsdfsfsdfasdasdasadasdasdasdasdasd", width: windowWidth, fontSize: 16, lineHeight: 25},
  {text: "Pennsylvania, bang có thể đưa Biden vượt mốc 270 phiếu đại cử tri để giành thắng lợi cuộc bầu cử Mỹ, có thể hoàn thành kiểm phiếu trong ngày 6/11, các quan chức bang cho hay. Biden hiện chỉ cách Trump chưa đầy 18.000 phiếu ở bang này. Trump từng dẫn trước tới nửa triệu phiếu vài giờ sau khi các điểm bỏ phiếu đóng cửa.", width: windowWidth, fontSize: 16, lineHeight: 25},
]
export default function App() {
  const [result, setResult] = React.useState<number[]>();

  React.useEffect(() => {
    TboxCommons.measure(texts).then((r)=> {
      console.log("height", r);
      setResult(r);
    })
  }, []);

  return (
    <View style={styles.container}>
      <ScrollView style={{marginTop: 50, flex:1}}>
      {result && result.map((size: any, index:  number) => { 
        /* console.log(index); */
        return <Text style={{width: texts[index].width ? texts[index].width : size, height: texts[index].width ? size : texts[index].height, fontSize:texts[index].fontSize, backgroundColor: randomColor()}}>{texts[index].text}</Text>
      })}
      {result && result.map((size: any, index:  number) => { 
        /* console.log(index); */
        return <Text onLayout={(e) => {
          console.log( index+ " " +  e.nativeEvent.layout.height);
        }}
          style={{flex: 1, fontSize:texts[index].fontSize, backgroundColor: randomColor(), lineHeight:25}}>{texts[index].text}</Text>
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
