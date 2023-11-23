module SmtpMacros
	def smtp
		Net::SMTP.start(MAILCATCHER_SMTP_HOST, MAILCATCHER_SMTP_PORT) do |smtp|
			yield smtp
		end
	end
end