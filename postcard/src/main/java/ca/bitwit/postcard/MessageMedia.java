
package ca.bitwit.postcard;

import android.media.Image;

import java.lang.Integer;
import java.nio.ByteBuffer;

import ca.bitwit.postcard.MessageAttachment;

public class MessageMedia extends MessageAttachment{

    public String mimeType;

    public String imageLocation = null;
    public ByteBuffer imageData;

    public String videoLocation;
    public ByteBuffer videoData;

    public MessageMedia(){}

    public void setupWithContentUrl(String urlString){
        /*
            self.progress = [NSProgress progressWithTotalUnitCount:100];
            NSURLRequest *request = [NSURLRequest requestWithURL:contentUrl];
            AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
            [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *op, id responseObject) {
                BWNetLog(@"Success");
                NSArray *imageTypes = @[
                        @"jpg",
                        @"png",
                        @"gif",
                ];

                BOOL isImage = NO;
                for(NSString *supportedType in imageTypes) {
                    if([supportedType caseInsensitiveCompare:contentUrl.pathExtension]) {
                        isImage = YES;
                        break;
                    }
                }

                if (isImage) {
                    //its an image
                    self.imageData = op.responseData;
                    self.image = [UIImage imageWithData:_imageData];
                    self.mimeType = [NSString stringWithFormat:@"image/%@", contentUrl.pathExtension];
                    self.progress.completedUnitCount = 100;
                } else {
                    self.videoData = op.responseData;
                    NSString *documentsDirectory = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
                    NSString *fullPath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"video.mp4"]]; //add our image to the path
                    [_videoData writeToFile:fullPath atomically:YES];

                    BWLog(@"Video path -> %@", fullPath);
                    AVURLAsset *avUrlAsset = [AVURLAsset URLAssetWithURL:[NSURL fileURLWithPath:fullPath] options:nil];
                    BWLog(@"AV URL Asset -> %@", avUrlAsset);

                    AVAssetImageGenerator *generator = [AVAssetImageGenerator assetImageGeneratorWithAsset:avUrlAsset];

                    NSArray *tracks = [avUrlAsset tracksWithMediaType:AVMediaTypeVideo];
                    AVAssetTrack *track = [tracks objectAtIndex:0];

                    generator.maximumSize = track.naturalSize;
                    generator.appliesPreferredTrackTransform = YES;
                    [generator generateCGImagesAsynchronouslyForTimes:@[[NSValue valueWithCMTime:kCMTimeZero]]
                                                    completionHandler:^(CMTime requestedTime, CGImageRef cgImage, CMTime actualTime, AVAssetImageGeneratorResult result, NSError *error) {
                                                        if (result == AVAssetImageGeneratorSucceeded) {
                                                            UIImage *newImage = [UIImage imageWithCGImage:cgImage];
                                                            self.image = newImage;
                                                            BWLog(@"Dropbox Video -- Image Asset created");
                                                            self.progress.completedUnitCount = 100;
                                                        }
                                                    }];
                }
            } failure:^(AFHTTPRequestOperation *op, NSError *error) {
                BWNetLog(@"Failure -- %@", error.description);
            }];

            [operation setDownloadProgressBlock:^(NSUInteger bytesRead, long long int totalBytesRead, long long int totalBytesExpectedToRead) {
                int completion = (int)(totalBytesExpectedToRead/totalBytesExpectedToRead * 90); //up to 90% of the completion
                self.progress.completedUnitCount = completion;
            }];
            [operation start];
        * */
    }

    public Integer length(){
       if (this.videoData != null){
           return this.videoData.capacity();
       } else {
           return this.imageData.capacity();
       }
    }

}