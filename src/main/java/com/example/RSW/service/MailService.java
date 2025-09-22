package com.example.RSW.service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.RSW.vo.ResultData;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

	@Autowired
	private JavaMailSender sender;

	// yml은 건드리지 않고, 여기서만 발신자 고정
	// ★★★ SendGrid에서 "Verified" 된 실제 발신 메일주소로 바꿔주세요 ★★★
	private static final String VERIFIED_FROM = "ryuj7338@gmail.com";

	@Value("${custom.emailFromName}")
	private String emailFromName;

	private static class MailHandler {
		private final JavaMailSender sender;
		private final MimeMessage message;
		private final MimeMessageHelper helper;

		public MailHandler(JavaMailSender sender) throws MessagingException {
			this.sender = sender;
			this.message = this.sender.createMimeMessage();
			this.helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
		}

		public void setFrom(String mail, String name) throws UnsupportedEncodingException, MessagingException {
			helper.setFrom(new InternetAddress(mail, name, StandardCharsets.UTF_8.name()));
		}

		public void setTo(String mail) throws MessagingException {
			helper.setTo(mail);
		}

		public void setSubject(String subject) throws MessagingException {
			helper.setSubject(subject);
		}

		public void setText(String html) throws MessagingException {
			helper.setText(html, true);
		}

		/** Envelope-From(Return-Path) 강제 */
		public void forceEnvelopeFrom(String mail) {
			if (sender instanceof JavaMailSenderImpl j) {
				Properties p = j.getJavaMailProperties();
				p.put("mail.smtp.from", mail); // Return-Path 로 사용됨
				j.setJavaMailProperties(p);
			}
		}

		public void send() {
			sender.send(message);
		}
	}

	/** 단순 발송 */
	public ResultData send(String to, String title, String html) {
		try {
			MailHandler mail = new MailHandler(sender);

			// 헤더 From 고정 (yml 값 무시)
			mail.setFrom(VERIFIED_FROM, emailFromName);

			// Envelope-From(Return-Path)도 동일 주소로 고정
			mail.forceEnvelopeFrom(VERIFIED_FROM);

			mail.setTo(to);
			mail.setSubject(title);
			mail.setText(html);
			mail.send();

			return ResultData.from("S-1", "메일이 발송되었습니다.");
		} catch (MailAuthenticationException e) {
			e.printStackTrace();
			return ResultData.from("F-AUTH",
					"메일 서버 인증 실패 (username=apikey / password=SendGrid API Key / Single Sender 검증 확인)");
		} catch (MailSendException e) {
			// SendGrid 550 문구를 잡아 보다 정확한 메시지 반환
			String msg = String.valueOf(e.getMessage());
			Throwable cause = e.getCause();
			String root = cause != null ? String.valueOf(cause.getMessage()) : "";
			if (msg.contains("550 The from address does not match")
					|| root.contains("550 The from address does not match")) {
				return ResultData.from("F-SENDER",
						"SendGrid 발신자 미인증: VERIFIED_FROM 주소를 SendGrid에서 인증(Verified)해야 합니다.");
			}
			e.printStackTrace();
			return ResultData.from("F-SEND", "메일 전송 실패 (수신자 주소/네트워크/포트 확인)");
		} catch (MessagingException e) {
			e.printStackTrace();
			return ResultData.from("F-MSG", "메일 구성 중 오류 (MessagingException)");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return ResultData.from("F-ENC", "인코딩 오류");
		} catch (Exception e) {
			e.printStackTrace();
			return ResultData.from("F-UNK", "알 수 없는 오류");
		}
	}
}
