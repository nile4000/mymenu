import {Image, SafeAreaView, StyleSheet, View, ViewStyle} from 'react-native';

interface Props {
  children?: React.ReactNode | React.ReactNode[];
  height: number;
  image?: string;
  style?: ViewStyle;
}

function Hero({image, height, children, style}: Props): JSX.Element {
  return (
    <View style={[style, {height}]}>
      <SafeAreaView style={styles.container}>
        {!!image && (
          <Image
            style={styles.image}
            source={{
              uri: image,
            }}
          />
        )}
        {children}
      </SafeAreaView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  image: {
    borderBottomEndRadius: 50,
    borderBottomStartRadius: 50,
    width: '100%',
    height: '100%',
    position: 'absolute', // Das Bild bleibt absolut positioniert
  },
});

export default Hero;
