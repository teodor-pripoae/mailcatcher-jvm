module SmtpMacros
  def smtp
    Net::SMTP.start(MAILCATCHER_SMTP_HOST, MAILCATCHER_SMTP_PORT) do |smtp|
      yield smtp
    end
  end

  def new_email(sender: Faker::Internet.email, recipient: Faker::Internet.email, subject: Faker::Lorem.sentence, body_plain: Faker::Lorem.paragraph, body_html: nil, attachments: [])
    message = Mail.new do
      to recipient
      from sender
      subject subject

      if body_plain && body_plain != ""
        text_part do
          body body_plain
        end
      end

      if body_html && body_html != ""
        html_part do
          content_type "text/html; charset=UTF-8"
          body body_html
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
