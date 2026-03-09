import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../../../core/theme/app_colors.dart';

class ProfilePhotoWidget extends StatelessWidget {
  final String? photoUrl;
  final String initials;
  final double size;
  final VoidCallback? onUpload;
  final bool isUploading;

  const ProfilePhotoWidget({
    super.key,
    this.photoUrl,
    required this.initials,
    this.size = 64,
    this.onUpload,
    this.isUploading = false,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        CircleAvatar(
          radius: size / 2,
          backgroundColor: AppColors.primary.withAlpha(26),
          foregroundColor: AppColors.primary,
          backgroundImage:
              photoUrl != null ? CachedNetworkImageProvider(photoUrl!) : null,
          child: photoUrl == null
              ? Text(
                  initials,
                  style: TextStyle(
                      fontSize: size * 0.4, fontWeight: FontWeight.bold),
                )
              : null,
        ),
        if (isUploading)
          Positioned.fill(
            child: Container(
              decoration: BoxDecoration(
                color: Colors.black.withAlpha(77),
                shape: BoxShape.circle,
              ),
              child: const Center(
                child: CircularProgressIndicator.adaptive(
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              ),
            ),
          ),
        if (onUpload != null && !isUploading)
          Positioned(
            bottom: 0,
            right: 0,
            child: InkWell(
              onTap: onUpload,
              child: Container(
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: AppColors.primary,
                  shape: BoxShape.circle,
                  border: Border.all(color: Colors.white, width: 2),
                ),
                child: const Icon(
                  Icons.camera_alt,
                  size: 16,
                  color: Colors.white,
                ),
              ),
            ),
          ),
      ],
    );
  }
}
