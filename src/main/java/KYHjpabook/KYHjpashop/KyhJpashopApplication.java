package KYHjpabook.KYHjpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KyhJpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(KyhJpashopApplication.class, args);
//		Hello hello = new Hello();
//		hello.setData("test");
//		String data = hello.getData();
//		System.out.println("data = " + data);
	}

	@Bean
	Hibernate5Module hibernate5Module() {
//		return new Hibernate5Module();  // no옵션 기본 세팅으로는 Jackson 라이브러리를 사용해서 반환할때, 지연로딩은 무시하고 null로 채워넣고 반환하게
		Hibernate5Module hibernate5Module = new Hibernate5Module();
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);  // Json생성하는 시점에 강제로 lazyLoading을 파파박 해버림
		return hibernate5Module;
	}

}