module SmtpMacros
  def smtp
    Net::SMTP.start(MAILCATCHER_SMTP_HOST, MAILCATCHER_SMTP_PORT) do |smtp|
      yield smtp
    end
  end

  def new_email(sender: Faker::Internet.email, recipient: Faker::Internet.email, subject: Faker::Lorem.sentence, bodyPlain: Faker::Lorem.paragraph, bodyHtml: nil, attachments: [])
    message = Mail.deliver do
      to recipient
      from sender
      subject subject

      if bodyPlain && bodyPlain != ""
        text_part do
          body bodyPlain
        end
      end

      if bodyHtml && bodyHtml != ""
        html_part do
          content_type "text/html; charset=UTF-8"
          body bodyHtml
        end
      end

      if attachments && attachments.size > 0
        attachments.each do |attachment|
          add_file attachment
        end
      end
    end
    message.to_s
  end
end
